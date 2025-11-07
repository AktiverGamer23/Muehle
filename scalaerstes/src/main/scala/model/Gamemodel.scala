package model


case class GameState(
  board: Board,
  currentPlayer: Player,
  whiteStonesToPlace: Int,
  blackStonesToPlace: Int,
  whiteStones: Int,
  blackStones: Int
)
enum Player:
  case White, Black
  def next: Player = this match
      case White => Black
      case Black => White

sealed trait Field
case object Empty extends Field
case class Occupied(player: Player) extends Field
type Board = Vector[Field]


val emptyBoard: Board = Vector.fill(24)(Empty)



// ===== Spiellogik =====
object Gamelogic:

  def placeStone(state: GameState, pos: Int): Option[GameState] =
    if pos < 0 || pos >= 24 then None
    else if state.board(pos) != Empty then None
    else
      val newBoard = state.board.updated(pos, Occupied(state.currentPlayer))
      val newState = state.currentPlayer match
        case Player.White =>
          state.copy(
            board = newBoard,
            whiteStonesToPlace = (state.whiteStonesToPlace - 1).max(0),
            whiteStones = state.whiteStones + 1,
            currentPlayer = Player.Black
          )
        case Player.Black =>
          state.copy(
            board = newBoard,
            blackStonesToPlace = (state.blackStonesToPlace - 1).max(0),
            blackStones = state.blackStones + 1,
            currentPlayer = Player.White
          )
      Some(newState)

  
  def validate(state: GameState): Option[Player] =
    if state.whiteStones < 3 then Some(Player.Black)
    else if state.blackStones < 3 then Some(Player.White)
    else None




 // def mill(board: Board,player: Player): boolean =
 //   val mills = List(
 //   List(0,1,2), List(2,3,4), List(4,5,6),List(6,7,0),List(8,9,10),List(10,11,12), List(12,13,14),List(14,15,8),
 //   List(16,17,18),List(18,19,20),List(20,21,22),List(22,23,16),List(1,9,17),List(3,11,19),List(5,13,21),List(7,15,23)
 // )
 // mills.exists()
