package de.htwg.se.muehle.fileio

import de.htwg.se.muehle.gamestate.{GameState, GameStateInterface}
import de.htwg.se.muehle.board.{Board, BoardInterface}
import de.htwg.se.muehle.player.Player
import de.htwg.se.muehle.model.*
import scala.util.{Try, Success, Failure}
import play.api.libs.json.*
import java.io.{File, PrintWriter}
import scala.io.Source

class JsonFileIO extends FileIOInterface:

  implicit val playerFormat: Format[Player] = new Format[Player] {
    def reads(json: JsValue): JsResult[Player] = json.validate[String].flatMap {
      case "White" => JsSuccess(Player.White)
      case "Black" => JsSuccess(Player.Black)
      case other => JsError(s"Unknown player: $other")
    }
    def writes(player: Player): JsValue = JsString(player.toString)
  }

  implicit val phaseFormat: Format[GameStateInterface.Phase] = new Format[GameStateInterface.Phase] {
    def reads(json: JsValue): JsResult[GameStateInterface.Phase] = json.validate[String].flatMap {
      case "PlacingPhase" => JsSuccess(GameStateInterface.PlacingPhase)
      case "MovingPhase" => JsSuccess(GameStateInterface.MovingPhase)
      case "MillRemovePhase" => JsSuccess(GameStateInterface.MillRemovePhase)
      case other => JsError(s"Unknown phase: $other")
    }
    def writes(phase: GameStateInterface.Phase): JsValue = JsString(phase match {
      case GameStateInterface.PlacingPhase => "PlacingPhase"
      case GameStateInterface.MovingPhase => "MovingPhase"
      case GameStateInterface.MillRemovePhase => "MillRemovePhase"
    })
  }

  implicit val messageFormat: Format[Message] = new Format[Message] {
    def reads(json: JsValue): JsResult[Message] = json.validate[String].flatMap {
      case "PlaceStoneSuccess" => JsSuccess(PlaceStoneMessage.Success)
      case "PlaceStoneInvalidMove" => JsSuccess(PlaceStoneMessage.InvalidMove)
      case "PlaceStoneOccupied" => JsSuccess(PlaceStoneMessage.Occupied)
      case "RemoveStoneSuccess" => JsSuccess(RemoveStoneMessage.Success)
      case "RemoveStoneInvalidMove" => JsSuccess(RemoveStoneMessage.InvalidMove)
      case "RemoveStoneOwnStoneChosen" => JsSuccess(RemoveStoneMessage.OwnStoneChosen)
      case "MoveStoneSuccess" => JsSuccess(MoveStoneMessage.Success)
      case "MoveStoneNoNeighbour" => JsSuccess(MoveStoneMessage.NoNeighbour)
      case "MoveStoneNeighbourOccupied" => JsSuccess(MoveStoneMessage.NeighbourOccupied)
      case "MoveStoneNotYourStone" => JsSuccess(MoveStoneMessage.NotYourStone)
      case "WinnerWhite" => JsSuccess(Winner.White)
      case "WinnerBlack" => JsSuccess(Winner.Black)
      case other => JsError(s"Unknown message: $other")
    }
    def writes(message: Message): JsValue = JsString(message match {
      case PlaceStoneMessage.Success => "PlaceStoneSuccess"
      case PlaceStoneMessage.InvalidMove => "PlaceStoneInvalidMove"
      case PlaceStoneMessage.Occupied => "PlaceStoneOccupied"
      case RemoveStoneMessage.Success => "RemoveStoneSuccess"
      case RemoveStoneMessage.InvalidMove => "RemoveStoneInvalidMove"
      case RemoveStoneMessage.OwnStoneChosen => "RemoveStoneOwnStoneChosen"
      case MoveStoneMessage.Success => "MoveStoneSuccess"
      case MoveStoneMessage.NoNeighbour => "MoveStoneNoNeighbour"
      case MoveStoneMessage.NeighbourOccupied => "MoveStoneNeighbourOccupied"
      case MoveStoneMessage.NotYourStone => "MoveStoneNotYourStone"
      case Winner.White => "WinnerWhite"
      case Winner.Black => "WinnerBlack"
      case _ => "Unknown"
    })
  }

  override def save(gameState: GameStateInterface, filePath: String): Try[Unit] = Try {
    val json = gameStateToJson(gameState)
    val writer = new PrintWriter(new File(filePath))
    try {
      writer.write(Json.prettyPrint(json))
    } finally {
      writer.close()
    }
  }

  override def load(filePath: String): Try[GameStateInterface] = Try {
    val source = Source.fromFile(filePath)
    try {
      val jsonString = source.mkString
      val json = Json.parse(jsonString)
      jsonToGameState(json)
    } finally {
      source.close()
    }
  }

  private def gameStateToJson(gameState: GameStateInterface): JsValue =
    Json.obj(
      "board" -> boardToJson(gameState.board),
      "currentPlayer" -> Json.toJson(gameState.currentPlayer),
      "phase" -> Json.toJson(gameState.phase),
      "whiteStonesToPlace" -> gameState.whiteStonesToPlace,
      "blackStonesToPlace" -> gameState.blackStonesToPlace,
      "whiteStones" -> gameState.whiteStones,
      "blackStones" -> gameState.blackStones,
      "message" -> gameState.message.map(Json.toJson(_))
    )

  private def boardToJson(board: BoardInterface): JsValue =
    val positions = (0 until 24).map { pos =>
      Json.obj(
        "index" -> pos,
        "player" -> board.stoneAt(pos).map(p => Json.toJson(p)).getOrElse(JsNull)
      )
    }
    Json.toJson(positions)

  private def jsonToGameState(json: JsValue): GameStateInterface =
    val board = jsonToBoard((json \ "board").as[JsArray])
    val currentPlayer = (json \ "currentPlayer").as[Player]
    val phase = (json \ "phase").as[GameStateInterface.Phase]
    val whiteStonesToPlace = (json \ "whiteStonesToPlace").as[Int]
    val blackStonesToPlace = (json \ "blackStonesToPlace").as[Int]
    val whiteStones = (json \ "whiteStones").as[Int]
    val blackStones = (json \ "blackStones").as[Int]
    val message = (json \ "message").asOpt[Message]

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

  private def jsonToBoard(positions: JsArray): BoardInterface =
    val positionsMap = positions.value.map { pos =>
      val index = (pos \ "index").as[Int]
      val player = (pos \ "player").asOpt[Player]
      index -> player
    }.toMap

    val vec1 = (0 to 7).map(i => positionsMap.getOrElse(i, None)).toVector
    val vec2 = (8 to 15).map(i => positionsMap.getOrElse(i, None)).toVector
    val vec3 = (16 to 23).map(i => positionsMap.getOrElse(i, None)).toVector

    Board(vec1, vec2, vec3)
