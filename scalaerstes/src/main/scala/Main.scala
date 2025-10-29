package scala
import scala.io.StdIn

// ------------------- Spieler -------------------
enum Player:
  case White, Black
  def next: Player = this match
    case White => Black
    case Black => White

// ------------------- Felder & Board -------------------
sealed trait Field
case object Empty extends Field
case class Occupied(player: Player) extends Field

type Board = Vector[Field]

// ------------------- Spielzustand -------------------
case class GameState(
  board: Board,
  currentPlayer: Player,
  whiteStonesToPlace: Int,
  blackStonesToPlace: Int,
  whiteStones: Int,
  blackStones: Int
)


val emptyBoard: Board = Vector.fill(24)(Empty)


def Feld(pos: Int, board: Board): String = board(pos) match {
  case Empty => "·"
  case Occupied(Player.White) => "W"
  case Occupied(Player.Black) => "B"
}

def renderBoard(board: Board, currentPlayer: Player): String =
  s"""
     ${Feld(16, board)}-----------${Feld(17, board)}-----------${Feld(18, board)}
     |           |           |
     |   ${Feld(8, board)}-------${Feld(9, board)}-------${Feld(10, board)}   |
     |   |       |       |   |
     |   |   ${Feld(0, board)}---${Feld(1, board)}---${Feld(2, board)}   |   |
     |   |   |       |   |   |
     ${Feld(23, board)}---${Feld(15, board)}---${Feld(7, board)}       ${Feld(3, board)}---${Feld(11, board)}---${Feld(19, board)}
     |   |   |       |   |   |
     |   |   ${Feld(6, board)}---${Feld(5, board)}---${Feld(4, board)}   |   |
     |   |       |       |   |
     |   ${Feld(14, board)}-------${Feld(13, board)}-------${Feld(12, board)}   |
     |           |           |
     ${Feld(22, board)}-----------${Feld(21, board)}-----------${Feld(20, board)}
  """


def placeStone(state: GameState, pos: Int): Option[GameState] = {
  if (state.board(pos) != Empty) None
  else {
    val newBoard = state.board.updated(pos, Occupied(state.currentPlayer))
    val newState = state.currentPlayer match {
      case Player.White => state.copy(board = newBoard, whiteStonesToPlace = state.whiteStonesToPlace - 1, currentPlayer = Player.Black)
      case Player.Black => state.copy(board = newBoard, blackStonesToPlace = state.blackStonesToPlace - 1, currentPlayer = Player.White)
    }
    Some(newState)
  }
}
def validate(state: GameState): Option[Player] = {
  if (state.whiteStones < 3) Some(Player.Black)  
  else if (state.blackStones < 3) Some(Player.White) 
  else None  
}


def Placing(state: GameState): GameState = {
  validate(state) match {
    case Some(winner) => println(s"Gewinner ist: $winner")
    case None =>
  }
  
  println(renderBoard(state.board, state.currentPlayer))
  if (state.whiteStonesToPlace == 0 && state.blackStonesToPlace == 0) {
    println("Platzierphase beendet!")
    state
  } else {
    println(s"${state.currentPlayer} ist am Zug. Wähle ein Feld (0-23):")
    val input = StdIn.readInt()
    placeStone(state, input) match {
      case Some(newState) => Placing(newState)
      case None =>
        println("Ungültiger Zug. Versuche es erneut.")
        Placing(state)
    }
  }
}

object MuehleApp {
  @main def runMuehle(): Unit = {
    println("Steine?")
    val steine = scala.io.StdIn.readInt()
    val initialState = GameState(emptyBoard, Player.White, steine, steine, steine, steine)
    Placing(initialState)
  }
}