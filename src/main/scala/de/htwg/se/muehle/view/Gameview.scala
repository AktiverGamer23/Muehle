package de.htwg.se.muehle.view

import de.htwg.se.muehle.gamestate.GameStateInterface
import de.htwg.se.muehle.controller.ControllerInterface
import de.htwg.se.muehle.model.*
import de.htwg.se.muehle.util.*
import scala.io.StdIn
import de.htwg.se.muehle.player.Player
import de.htwg.se.muehle.board.BoardInterface

class GameView(controller: ControllerInterface) extends Observer:

  controller.add(this)

  def start(): Unit =
    println("Spiel gestartet! (u = Undo, r = Redo, s = Save, l = Load, q = Quit)")

    var running = true
    while running do
      val gs = controller.getGameState

      printBoard(gs.board)
      printGameStatus(gs)
      gs.message.foreach(m => println(s"→ $m"))

      gs.message match
        case Some(Winner.White) =>
          println("=" * 50)
          println("🎉 SPIEL VORBEI! Gewinner: WEISS 🎉")
          println("=" * 50)
          running = handleGameOver()
        case Some(Winner.Black) =>
          println("=" * 50)
          println("🎉 SPIEL VORBEI! Gewinner: SCHWARZ 🎉")
          println("=" * 50)
          running = handleGameOver()
        case _ =>
          running = handlePlayerInput(gs)

  override def update: Unit =
    val gs = controller.getGameState
    printBoard(gs.board)
    printGameStatus(gs)

  private def handlePlayerInput(gs: GameStateInterface): Boolean =
    print("> ")
    val input = StdIn.readLine()

    input match
      case "u" =>
        controller.undo()
        true
      case "r" =>
        controller.redo()
        true
      case "s" =>
        print("Dateiname zum Speichern: ")
        val filename = StdIn.readLine()
        controller.save(filename) match
          case scala.util.Success(_) => println(s"✓ Spiel erfolgreich gespeichert in $filename")
          case scala.util.Failure(e) => println(s"✗ Fehler beim Speichern: ${e.getMessage}")
        true
      case "l" =>
        print("Dateiname zum Laden: ")
        val filename = StdIn.readLine()
        controller.load(filename) match
          case scala.util.Success(_) => println(s"✓ Spiel erfolgreich geladen aus $filename")
          case scala.util.Failure(e) => println(s"✗ Fehler beim Laden: ${e.getMessage}")
        true
      case "q" =>
        println("Beende Spiel.")
        false
      case _ =>
        gs.phase match
          case GameStateInterface.PlacingPhase =>
            parseInt(input) match
              case Some(pos) => controller.handle(pos); true
              case None      => println("Ungültige Eingabe!"); true
          case GameStateInterface.MovingPhase =>
            val parts = input.split(" ")
            if parts.length != 2 then
              println("Bitte Eingabe: from to (z.B. 3 10)"); true
            else
              (parseInt(parts(0)), parseInt(parts(1))) match
                case (Some(from), Some(to)) => controller.handle(from, to); true
                case _ => println("Ungültige Eingabe!"); true
          case GameStateInterface.MillRemovePhase =>
            parseInt(input) match
              case Some(pos) => controller.handle(pos); true
              case None      => println("Ungültige Eingabe!"); true

  private def parseInt(s: String): Option[Int] =
    try Some(s.toInt)
    catch case _: Throwable => None

  private def handleGameOver(): Boolean =
    print("Möchten Sie nochmal spielen? (j/n): ")
    val input = StdIn.readLine().toLowerCase

    input match
      case "j" | "ja" | "y" | "yes" =>
        controller.restart()
        true
      case _ =>
        println("Beende Spiel. Auf Wiedersehen!")
        false

  private def printGameStatus(gs: GameStateInterface): Unit =
    println("=" * 50)
    println(s"Aktueller Spieler: ${gs.currentPlayer}")
    println(s"Phase: ${gs.phase}")
    println(s"WEISS - Zu platzieren: ${gs.whiteStonesToPlace}, Auf dem Brett: ${gs.whiteStones}")
    println(s"SCHWARZ - Zu platzieren: ${gs.blackStonesToPlace}, Auf dem Brett: ${gs.blackStones}")
    println("=" * 50)

  private def printBoard(board: BoardInterface): Unit =
    def stoneToChar(opt: Option[Player]): Char =
      opt match
        case Some(Player.White) => 'W'
        case Some(Player.Black) => 'B'
        case None               => '.'

    val chars = (0 until 24).map(pos => stoneToChar(board.stoneAt(pos)))

    println(s"${chars(0)}---------${chars(1)}---------${chars(2)}")
    println(s"|         |         |")
    println(s"|  ${chars(3)}------${chars(4)}------${chars(5)}  |")
    println(s"|  |      |      |  |")
    println(s"|  |  ${chars(6)}---${chars(7)}---${chars(8)}  |  |")
    println(s"|  |  |       |  |  |")
    println(s"${chars(9)}--${chars(10)}-${chars(11)}       ${chars(12)}-${chars(13)}--${chars(14)}")
    println(s"|  |  |       |  |  |")
    println(s"|  |  ${chars(15)}---${chars(16)}---${chars(17)}  |  |")
    println(s"|  |      |      |  |")
    println(s"|  ${chars(18)}------${chars(19)}------${chars(20)}  |")
    println(s"|         |         |")
    println(s"${chars(21)}---------${chars(22)}---------${chars(23)}")