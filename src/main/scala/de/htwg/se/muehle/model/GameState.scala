package de.htwg.se.muehle.model

import de.htwg.se.muehle.util.*

case class GameState(
    board: Board = Board.empty,
    currentPlayer: Player = Player.White,
    whiteStonesToPlace: Int = 9,
    blackStonesToPlace: Int = 9,
    whiteStones: Int = 0,
    blackStones: Int = 0,
    message: Option[Message] = None
):
  private def getPlayer(board: Board,pos: Int): Option[Player] =
    val vec = pos / 8
    val idx = pos % 8
    vec match
      case 0 => board.vec1(idx)
      case 1 => board.vec2(idx)
      case 2 => board.vec3(idx)

  def placeStone(pos: Int): GameState =
    if whiteStonesToPlace == 0 && blackStonesToPlace == 0 then copy(message = Some(SuccessMessage.MoveStone))
    else if pos < 0 || pos >= 24 then copy(message = Some(ErrorMessage.InvalidMove))
    else getPlayer(board, pos) match
      case Some(_) => copy(message = Some(ErrorMessage.InvalidMove))
      case None =>
        val newBoard = Board.set(board, pos, Some(currentPlayer))
        val (newWhite, newBlack) = currentPlayer match
          case Player.White => (whiteStonesToPlace - 1, blackStonesToPlace)
          case Player.Black => (whiteStonesToPlace, blackStonesToPlace - 1)
        val newState = copy(board = newBoard, whiteStonesToPlace = newWhite, blackStonesToPlace = newBlack)
        if isMill(newBoard, pos) then 
          println("Mühle") 
          newState.copy(message = Some(SuccessMessage.RemoveStone))
        else 
          newState.copy(currentPlayer = currentPlayer.next, message = Some(SuccessMessage.PlaceStone))

  def removeStone(pos: Int): GameState =
    if pos < 0 || pos >= 24 then copy(message = Some(ErrorMessage.InvalidMoveRemove))
      else getPlayer(board,pos) match
        case Some(owner) if owner == currentPlayer =>
          copy(message = Some(ErrorMessage.OwnStoneChosen))
        case Some(owner) =>
          val newBoard = Board.set(board, pos, None)
          val msg =
            if whiteStonesToPlace == 0 && blackStonesToPlace == 0 then SuccessMessage.MoveStone
            else SuccessMessage.PlaceStone
          copy(board = newBoard, currentPlayer = currentPlayer.next, message = Some(msg))
        case None => copy(message = Some(ErrorMessage.InvalidMoveRemove))

  def moveStone(from: Int, to: Int): GameState =
    if from < 0 || from >= 24 || to < 0 || to >= 24 then copy(message = Some(ErrorMessage.InvalidMove))
    else (getPlayer(board, from), getPlayer(board, to)) match
      case (Some(owner), None) if owner == currentPlayer =>
        if !isNeighbour(from, to) then copy(message = Some(ErrorMessage.NoNeighbour))
        else
          val clearedBoard = Board.set(board, from, None)
          val newBoard = Board.set(clearedBoard, to, Some(currentPlayer))
          val newState = copy(board = newBoard)
          if isMill(newBoard, to) then newState.copy(message = Some(SuccessMessage.RemoveStone))
          else newState.copy(currentPlayer = currentPlayer.next, message = Some(SuccessMessage.PlaceStone))
      case (Some(_), Some(_)) => copy(message = Some(ErrorMessage.NeighbourOccupied))
      case (Some(_), None) => copy(message = Some(ErrorMessage.NoNeighbour))
      case (None, _) => copy(message = Some(ErrorMessage.OwnStoneChosen))

  def isNeighbour(from: Int, to: Int): Boolean =
    val fromVec = from / 8
    val fromIdx = from % 8
    val toVec = to / 8
    val toIdx = to % 8
    val invec = (fromVec == toVec) && ((fromIdx + 1) % 8 == toIdx || (fromIdx + 7) % 8 == toIdx)
    val ovec = (fromIdx % 2 != 0) && (math.abs(fromVec - toVec) == 1) && (fromIdx == toIdx)
    invec || ovec

  def isMill(board: Board, pos: Int): Boolean =
    val vec = pos / 8
    val idx = pos % 8
    getPlayer(board, pos).exists { player =>
      val ring = vec match
        case 0 => board.vec1
        case 1 => board.vec2
        case 2 => board.vec3
      val i1 = (idx + 1) % 8
      val i2 = (idx + 2) % 8
      val i7 = (idx + 7) % 8
      val i6 = (idx + 6) % 8
      if idx % 2 == 0 then
        (ring(i1).contains(player) && ring(i2).contains(player)) ||
        (ring(i7).contains(player) && ring(i6).contains(player))
      else
        (ring(i1).contains(player) && ring(i7).contains(player)) ||
        (board.vec1(idx).contains(player) && board.vec2(idx).contains(player) && board.vec3(idx).contains(player))
    }


trait GamePhase
case object Placing extends GamePhase
case object Moving extends GamePhase
case object Removing extends GamePhase

object GameStateContext {

  var phase: GamePhase = Placing

  def handle(pos1: Int, pos2: Int, game: GameState): GameState = {
    phase match
      case Placing  => placingState(game, pos1)
      case Moving   => movingState(game, pos1, pos2)
      case Removing => removingState(game, pos1)
  }

  private def placingState(game: GameState, pos: Int): GameState = {
    val newGame = game.placeStone(pos)

    val millFormed = newGame.isMill(newGame.board, pos)

     phase = if millFormed then Removing
      else if newGame.whiteStonesToPlace == 0 && newGame.blackStonesToPlace == 0 then Moving
      else Placing

    newGame
  }

  private def movingState(game: GameState, from: Int, to: Int): GameState = {
    val newGame = game.moveStone(from, to)

    val millFormed = newGame.isMill(newGame.board, to)

     phase = if millFormed then Removing
      else if newGame.whiteStonesToPlace == 0 && newGame.blackStonesToPlace == 0 then Moving
      else Placing

    newGame
  }

  private def removingState(game: GameState, pos: Int): GameState = {
    val newGame = game.removeStone(pos)

    val wasSuccessful = newGame.message.exists {
      case SuccessMessage.PlaceStone | SuccessMessage.MoveStone => true
      case _ => false
    }

    if !wasSuccessful then
      phase = Removing
    else if newGame.whiteStonesToPlace == 0 && newGame.blackStonesToPlace == 0 then
      phase = Moving
    else
      phase = Placing

    newGame
  }
}
