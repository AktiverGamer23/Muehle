package de.htwg.se.muehle.view

import de.htwg.se.muehle.controller.ControllerInterface
import de.htwg.se.muehle.util.Observer
import de.htwg.se.muehle.model._
import de.htwg.se.muehle.player.Player
import de.htwg.se.muehle.gamestate.GameStateInterface

import scala.swing._
import scala.swing.event._
import java.awt.{Color, Font, Graphics2D, BasicStroke, RenderingHints, Point}
import java.awt.geom.Ellipse2D
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class Gui(controller: ControllerInterface) extends Observer {

  controller.add(this)

  private val messageLabel = new Label("Spiel gestartet!") {
    font = new Font("Arial", Font.BOLD, 18)
    foreground = Color.DARK_GRAY
    horizontalAlignment = Alignment.Center
  }
  private val statusLabel = new Label {
    font = new Font("Arial", Font.PLAIN, 14)
    foreground = Color.DARK_GRAY
    horizontalAlignment = Alignment.Center
  }

  private val boardPanel = new BoardPanel(controller)

  private val saveButton = new Button("Save") {
    reactions += {
      case ButtonClicked(_) =>
        val dialog = new FileChooser()
        dialog.title = "Save Game"
        val result = dialog.showSaveDialog(null)
        if (result == FileChooser.Result.Approve) {
          controller.save(dialog.selectedFile.getAbsolutePath) match
            case scala.util.Success(_) =>
              messageLabel.text = s"Saved to ${dialog.selectedFile.getName}"
            case scala.util.Failure(e) =>
              messageLabel.text = s"Save failed: ${e.getMessage}"
        }
    }
  }

  private val loadButton = new Button("Load") {
    reactions += {
      case ButtonClicked(_) =>
        val dialog = new FileChooser()
        dialog.title = "Load Game"
        val result = dialog.showOpenDialog(null)
        if (result == FileChooser.Result.Approve) {
          controller.load(dialog.selectedFile.getAbsolutePath) match
            case scala.util.Success(_) =>
              messageLabel.text = s"Loaded from ${dialog.selectedFile.getName}"
            case scala.util.Failure(e) =>
              messageLabel.text = s"Load failed: ${e.getMessage}"
        }
    }
  }

  private val undoButton = new Button("Undo") {
    reactions += {
      case ButtonClicked(_) =>
        controller.undo()
        messageLabel.text = "Undo"
    }
  }

  private val redoButton = new Button("Redo") {
    reactions += {
      case ButtonClicked(_) =>
        controller.redo()
        messageLabel.text = "Redo"
    }
  }

  def start(): Unit = Swing.onEDT {
    new MainFrame {
      title = "Mühle"
      contents = new BorderPanel {
        border = Swing.EmptyBorder(12, 12, 12, 12)
        layout(new BoxPanel(Orientation.Vertical) {
          contents += messageLabel
          contents += statusLabel
        }) = BorderPanel.Position.North
        layout(boardPanel) = BorderPanel.Position.Center
        layout(new FlowPanel {
          contents += undoButton
          contents += redoButton
          contents += Swing.HStrut(20)
          contents += saveButton
          contents += loadButton
        }) = BorderPanel.Position.South
      }
      minimumSize = new Dimension(520, 600)
      size = new Dimension(560, 660)
      centerOnScreen()
      visible = true
    }
  }

  override def update: Unit = Swing.onEDT {
    val gs = controller.getGameState

    // Check for winner and display special message
    gs.message match {
      case Some(de.htwg.se.muehle.model.Winner.White) =>
        messageLabel.text = "🎉 GAME OVER! WINNER: WHITE 🎉"
        messageLabel.foreground = Color.BLUE
        showGameOverDialog("WEISS")
      case Some(de.htwg.se.muehle.model.Winner.Black) =>
        messageLabel.text = "🎉 GAME OVER! WINNER: BLACK 🎉"
        messageLabel.foreground = Color.RED
        showGameOverDialog("SCHWARZ")
      case other =>
        messageLabel.text = other.map(_.toString).getOrElse("")
        messageLabel.foreground = Color.DARK_GRAY
    }

    statusLabel.text =
      s"White to place: ${gs.whiteStonesToPlace}, Black to place: ${gs.blackStonesToPlace} | " +
      s"White stones: ${gs.whiteStones}, Black stones: ${gs.blackStones} | " +
      s"Current player: ${gs.currentPlayer}, Phase: ${gs.phase}"

    boardPanel.repaint()
  }

  private def showGameOverDialog(winner: String): Unit = {
    val result = Dialog.showConfirmation(
      boardPanel,
      s"Gewinner: $winner!\n\nMöchten Sie nochmal spielen?",
      "Spiel beendet",
      Dialog.Options.YesNo,
      Dialog.Message.Question
    )

    result match {
      case Dialog.Result.Yes =>
        controller.restart()
      case Dialog.Result.No =>
        sys.exit(0)
      case _ =>
    }
  }


  private class BoardPanel(controller: ControllerInterface) extends Panel {

    preferredSize = new Dimension(520, 520)
    background = Color.WHITE

    private val margin = 40
    private val pointRadius = 14
    private val clickRadius = 20

    private val positionsIndexToGrid: Map[Int, (Int, Int)] = Map(
      16  -> (0,0), 17  -> (3,0), 18 -> (6,0),
      8  -> (1,1), 9  -> (3,1), 10  -> (5,1),
      20  -> (6,6), 1  -> (3,2), 2  -> (4,2),
      23  -> (0,3),15 -> (1,3),7 -> (2,3),
      3 -> (4,3),11 -> (5,3),19 -> (6,3),
      6 -> (2,4),5 -> (3,4),4 -> (4,4),
      14 -> (1,5),13 -> (3,5),12 -> (5,5),
      22 -> (0,6),21 -> (3,6),0 -> (2,2)
    )

    @volatile private var pixelCoords: Map[Int, Point] = Map.empty

    private var movingFrom: Option[Int] = None

    listenTo(mouse.clicks)
    reactions += {
      case e: MouseClicked =>
        if (pixelCoords.isEmpty) pixelCoords = computePointCoords(size)
        val click = e.point
        val nearest = pixelCoords.minBy{ case (_, p) => distanceSquared(p, click) }
        val (index, point) = nearest
        if (distanceSquared(point, click) <= clickRadius * clickRadius) {
          controller.getGameState.phase match {
            case GameStateInterface.PlacingPhase =>
              controller.handle(index) 

            case GameStateInterface.MovingPhase =>
              movingFrom match {
                case None => movingFrom = Some(index) 
                case Some(from) =>
                  Future { controller.handle(from, index) }
                  movingFrom = None 
              }

            case GameStateInterface.MillRemovePhase =>
              controller.handle(index)
          }
        }
    }

    override def paintComponent(g: Graphics2D): Unit = {
      super.paintComponent(g)
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      pixelCoords = computePointCoords(size)
      drawBoardLines(g)
      drawPoints(g)
      drawStones(g)

      movingFrom.foreach { fromIdx =>
        pixelCoords.get(fromIdx).foreach { p =>
          g.setColor(new Color(255, 255, 0, 120))
          g.fillOval(p.x - pointRadius, p.y - pointRadius, pointRadius*2, pointRadius*2)
        }
      }
    }


    private def drawBoardLines(g: Graphics2D): Unit = {
      g.setStroke(new BasicStroke(3f))
      g.setColor(Color.DARK_GRAY)

      def pointByGrid(grid: (Int,Int)): Point =
        pixelCoords.find { case (idx, _) => positionsIndexToGrid(idx) == grid }.get._2

      val p00 = pointByGrid((0,0)); val p60 = pointByGrid((6,0))
      val p66 = pointByGrid((6,6)); val p06 = pointByGrid((0,6))
      g.drawLine(p00.x,p00.y,p60.x,p60.y)
      g.drawLine(p60.x,p60.y,p66.x,p66.y)
      g.drawLine(p66.x,p66.y,p06.x,p06.y)
      g.drawLine(p06.x,p06.y,p00.x,p00.y)

      val p11 = pointByGrid((1,1)); val p51 = pointByGrid((5,1))
      val p55 = pointByGrid((5,5)); val p15 = pointByGrid((1,5))
      g.drawLine(p11.x,p11.y,p51.x,p51.y)
      g.drawLine(p51.x,p51.y,p55.x,p55.y)
      g.drawLine(p55.x,p55.y,p15.x,p15.y)
      g.drawLine(p15.x,p15.y,p11.x,p11.y)

      val p22 = pointByGrid((2,2)); val p42 = pointByGrid((4,2))
      val p44 = pointByGrid((4,4)); val p24 = pointByGrid((2,4))
      g.drawLine(p22.x,p22.y,p42.x,p42.y)
      g.drawLine(p42.x,p42.y,p44.x,p44.y)
      g.drawLine(p44.x,p44.y,p24.x,p24.y)
      g.drawLine(p24.x,p24.y,p22.x,p22.y)

      g.drawLine(pointByGrid((0,3)).x,pointByGrid((0,3)).y,pointByGrid((2,3)).x,pointByGrid((2,3)).y)
      g.drawLine(pointByGrid((6,3)).x,pointByGrid((6,3)).y,pointByGrid((4,3)).x,pointByGrid((4,3)).y)
      g.drawLine(pointByGrid((3,0)).x,pointByGrid((3,0)).y,pointByGrid((3,2)).x,pointByGrid((3,2)).y)
      g.drawLine(pointByGrid((3,6)).x,pointByGrid((3,6)).y,pointByGrid((3,4)).x,pointByGrid((3,4)).y)
    }


    private def drawPoints(g: Graphics2D): Unit = {
      g.setStroke(new BasicStroke(2f))
      g.setColor(Color.GRAY)
      for ((_, p) <- pixelCoords.toSeq.sortBy(_._1)) {
        val r = pointRadius
        g.drawOval(p.x - r, p.y - r, r*2, r*2)
      }
    }


    private def drawStones(g: Graphics2D): Unit = {
      for (i <- 0 until 24) {
        val playerOpt = controller.getGameState.board.stoneAt(i)
        val p = pixelCoords.getOrElse(i, new Point(margin, margin))
        playerOpt match {
          case Some(Player.Black) => drawStone(g,p.x,p.y,pointRadius,Color.BLACK)
          case Some(Player.White) => drawStone(g,p.x,p.y,pointRadius,new Color(40,90,200))
          case None => ()
        }
      }
    }

    private def drawStone(g: Graphics2D, cx:Int, cy:Int, r:Int, color: Color): Unit = {
      val shadowOffset = 2
      g.setColor(new Color(0,0,0,40))
      g.fill(new Ellipse2D.Double(cx-r+shadowOffset, cy-r+shadowOffset, r*2, r*2))
      g.setColor(color)
      g.fill(new Ellipse2D.Double(cx-r, cy-r, r*2, r*2))
      g.setColor(new Color(255,255,255,80))
      g.fill(new Ellipse2D.Double(cx-r+r/3, cy-r+r/3, r/2, r/2))
      g.setStroke(new BasicStroke(2f))
      g.setColor(new Color(30,30,30,120))
      g.draw(new Ellipse2D.Double(cx-r, cy-r, r*2, r*2))
    }


    private def computePointCoords(sz: Dimension): Map[Int, Point] = {
      val minSide = Math.min(sz.width, sz.height)
      val size = Math.max(minSide - margin*2, 200)
      val cell = size.toDouble / 6.0
      val offsetX = (sz.width - size) / 2
      val offsetY = (sz.height - size) / 2
      positionsIndexToGrid.map{ case (idx,(gx,gy)) =>
        idx -> new Point(Math.round(offsetX + gx*cell).toInt, Math.round(offsetY + gy*cell).toInt)
      }
    }

    private def distanceSquared(p1: Point, p2: Point): Int = {
      val dx = p1.x - p2.x
      val dy = p1.y - p2.y
      dx*dx + dy*dy
    }

  }
}
