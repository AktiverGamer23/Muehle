package de.htwg.se.muehle.fileio

import de.htwg.se.muehle.gamestate.{GameState, GameStateInterface}
import de.htwg.se.muehle.board.{Board, BoardInterface}
import de.htwg.se.muehle.player.Player
import de.htwg.se.muehle.model.*
import scala.util.{Try, Success, Failure}
import scala.xml.*
import java.io.{File, PrintWriter}

class XmlFileIO extends FileIOInterface:

  override def save(gameState: GameStateInterface, filePath: String): Try[Unit] = Try {
    val xml = gameStateToXml(gameState)
    val prettyPrinter = new PrettyPrinter(80, 2)
    val writer = new PrintWriter(new File(filePath))
    try {
      writer.write(prettyPrinter.format(xml))
    } finally {
      writer.close()
    }
  }

  override def load(filePath: String): Try[GameStateInterface] = Try {
    val xml = XML.loadFile(filePath)
    xmlToGameState(xml)
  }

  private def gameStateToXml(gameState: GameStateInterface): Elem =
    <gameState>
      <board>
        {boardToXml(gameState.board)}
      </board>
      <currentPlayer>{gameState.currentPlayer.toString}</currentPlayer>
      <phase>{phaseToString(gameState.phase)}</phase>
      <whiteStonesToPlace>{gameState.whiteStonesToPlace}</whiteStonesToPlace>
      <blackStonesToPlace>{gameState.blackStonesToPlace}</blackStonesToPlace>
      <whiteStones>{gameState.whiteStones}</whiteStones>
      <blackStones>{gameState.blackStones}</blackStones>
      <message>{messageToString(gameState.message)}</message>
    </gameState>

  private def boardToXml(board: BoardInterface): NodeSeq =
    (0 until 24).map { pos =>
      <position index={pos.toString}>
        {board.stoneAt(pos).map(_.toString).getOrElse("Empty")}
      </position>
    }

  private def xmlToGameState(xml: Node): GameStateInterface =
    val board = xmlToBoard((xml \ "board" \ "position").asInstanceOf[NodeSeq])
    val currentPlayer = Player.valueOf((xml \ "currentPlayer").text)
    val phase = stringToPhase((xml \ "phase").text)
    val whiteStonesToPlace = (xml \ "whiteStonesToPlace").text.toInt
    val blackStonesToPlace = (xml \ "blackStonesToPlace").text.toInt
    val whiteStones = (xml \ "whiteStones").text.toInt
    val blackStones = (xml \ "blackStones").text.toInt
    val message = stringToMessage((xml \ "message").text)

    GameState(
      board = board,
      currentPlayer = currentPlayer,
      phase = phase,
      whiteStonesToPlace = whiteStonesToPlace,
      blackStonesToPlace = blackStonesToPlace,
      whiteStones = whiteStones,
      blackStones = blackStones,
      message = message
    )

  private def xmlToBoard(positions: NodeSeq): BoardInterface =
    val positionsMap = positions.map { node =>
      val index = (node \ "@index").text.toInt
      val player = node.text.trim match
        case "White" => Some(Player.White)
        case "Black" => Some(Player.Black)
        case _ => None
      index -> player
    }.toMap

    val vec1 = (0 to 7).map(i => positionsMap.getOrElse(i, None)).toVector
    val vec2 = (8 to 15).map(i => positionsMap.getOrElse(i, None)).toVector
    val vec3 = (16 to 23).map(i => positionsMap.getOrElse(i, None)).toVector

    Board(vec1, vec2, vec3)

  private def phaseToString(phase: GameStateInterface.Phase): String = phase match
    case GameStateInterface.PlacingPhase => "PlacingPhase"
    case GameStateInterface.MovingPhase => "MovingPhase"
    case GameStateInterface.MillRemovePhase => "MillRemovePhase"

  private def stringToPhase(str: String): GameStateInterface.Phase = str match
    case "PlacingPhase" => GameStateInterface.PlacingPhase
    case "MovingPhase" => GameStateInterface.MovingPhase
    case "MillRemovePhase" => GameStateInterface.MillRemovePhase
    case _ => GameStateInterface.PlacingPhase

  private def messageToString(message: Option[Message]): String = message match
    case Some(PlaceStoneMessage.Success) => "PlaceStoneSuccess"
    case Some(PlaceStoneMessage.InvalidMove) => "PlaceStoneInvalidMove"
    case Some(PlaceStoneMessage.Occupied) => "PlaceStoneOccupied"
    case Some(RemoveStoneMessage.Success) => "RemoveStoneSuccess"
    case Some(RemoveStoneMessage.InvalidMove) => "RemoveStoneInvalidMove"
    case Some(RemoveStoneMessage.OwnStoneChosen) => "RemoveStoneOwnStoneChosen"
    case Some(MoveStoneMessage.Success) => "MoveStoneSuccess"
    case Some(MoveStoneMessage.NoNeighbour) => "MoveStoneNoNeighbour"
    case Some(MoveStoneMessage.NeighbourOccupied) => "MoveStoneNeighbourOccupied"
    case Some(MoveStoneMessage.NotYourStone) => "MoveStoneNotYourStone"
    case Some(Winner.White) => "WinnerWhite"
    case Some(Winner.Black) => "WinnerBlack"
    case None => "None"
    case _ => "Unknown"

  private def stringToMessage(str: String): Option[Message] = str match
    case "PlaceStoneSuccess" => Some(PlaceStoneMessage.Success)
    case "PlaceStoneInvalidMove" => Some(PlaceStoneMessage.InvalidMove)
    case "PlaceStoneOccupied" => Some(PlaceStoneMessage.Occupied)
    case "RemoveStoneSuccess" => Some(RemoveStoneMessage.Success)
    case "RemoveStoneInvalidMove" => Some(RemoveStoneMessage.InvalidMove)
    case "RemoveStoneOwnStoneChosen" => Some(RemoveStoneMessage.OwnStoneChosen)
    case "MoveStoneSuccess" => Some(MoveStoneMessage.Success)
    case "MoveStoneNoNeighbour" => Some(MoveStoneMessage.NoNeighbour)
    case "MoveStoneNeighbourOccupied" => Some(MoveStoneMessage.NeighbourOccupied)
    case "MoveStoneNotYourStone" => Some(MoveStoneMessage.NotYourStone)
    case "WinnerWhite" => Some(Winner.White)
    case "WinnerBlack" => Some(Winner.Black)
    case "None" => None
    case _ => None
