package de.htwg.se.muehle.model

import de.htwg.se.muehle.util.*


case class GameState(
  board: Board,
  currentPlayer: Player,
  whiteStonesToPlace: Int,
  blackStonesToPlace: Int,
  whiteStones: Int,
  blackStones: Int,
  message: Option[Message] = None
):

  private def isOwnStone(pos: Int): Boolean =
    board(pos) match
      case Occupied(p) if p == currentPlayer => true
      case _ => false

  private def isNeighbour(from: Int, to: Int): Boolean =
    neighbors(from).contains(to)

  private[model] def canFly(player: Player): Boolean =
    player match
      case Player.White => whiteStones == 3
      case Player.Black => blackStones == 3

  def isMillFormed(pos: Int): Boolean =
    mills.exists(mill =>
      mill.contains(pos) &&
      mill.forall(i => board(i) == Occupied(currentPlayer))
    )

  def isPlacingPhaseOver: Boolean =
    whiteStonesToPlace == 0 && blackStonesToPlace == 0

  def validate: Option[Player] =
    if whiteStones < 3 then Some(Player.Black)
    else if blackStones < 3 then Some(Player.White)
    else None

  def switchPlayer: GameState =
    copy(currentPlayer = currentPlayer.next)


  /* ──────────────────────────────────────────────────────────────
   * STONE PLACEMENT
   * ────────────────────────────────────────────────────────────── */
  def placeStone(pos: Int): GameState =
    board(pos) match
      case Empty =>
        val newBoard = board.updated(pos, Occupied(currentPlayer))

      // Steine setzen, aber die Gesamtzahl der Steine nicht erhöhen
        val afterPlace = currentPlayer match
          case Player.White =>
            copy(board = newBoard, whiteStonesToPlace = (whiteStonesToPlace - 1).max(0))
          case Player.Black =>
            copy(board = newBoard, blackStonesToPlace = (blackStonesToPlace - 1).max(0))

      // Prüfen, ob Mühle entstanden ist
        if afterPlace.isMillFormed(pos) then
          afterPlace.copy(message = Some(SuccessMessage.RemoveStone))
        else
        // Spieler wechseln
          val nextPlayerState = afterPlace.switchPlayer

        // Prüfen, ob Placing-Phase vorbei für beide Spieler
          val nextMessage =
            if afterPlace.whiteStonesToPlace == 0 && afterPlace.blackStonesToPlace == 0 then
              SuccessMessage.MoveStone   // Wechsel automatisch zur Move-Phase
            else
              SuccessMessage.PlaceStone

          nextPlayerState.copy(message = Some(nextMessage))

      case _ =>
        copy(message = Some(ErrorMessage.InvalidMove))



  /* ──────────────────────────────────────────────────────────────
   * STONE MOVEMENT
   * ────────────────────────────────────────────────────────────── */
  def moveStone(from: Int, to: Int): GameState =

    if !isOwnStone(from) then
      return copy(message = Some(ErrorMessage.NotYourStone))

    if !isNeighbour(from, to) && !canFly(currentPlayer) then
      return copy(message = Some(ErrorMessage.NoNeighbour))

    board(to) match
      case Empty =>
        val newBoard =
          board.updated(from, Empty)
               .updated(to, Occupied(currentPlayer))

        val moved = copy(board = newBoard)

        if moved.isMillFormed(to) then
          moved.copy(message = Some(SuccessMessage.RemoveStone))
        else
          moved
            .copy(message = Some(SuccessMessage.MoveStone))
            .switchPlayer

      case _ =>
        copy(message = Some(ErrorMessage.NeighbourOccupied))



  def removeStone(pos: Int): GameState =
    board(pos) match
      case Occupied(p) if p != currentPlayer =>
        val newBoard = board.updated(pos, Empty)

        val after =
          p match
          case Player.White =>
            copy(board = newBoard, whiteStones = (whiteStones - 1).max(0))
          case Player.Black =>
            copy(board = newBoard, blackStones = (blackStones - 1).max(0))

        val nextMessage =
          if isPlacingPhaseOver then
            SuccessMessage.MoveStone
          else
            SuccessMessage.PlaceStone

        after
          .copy(message = Some(nextMessage))
          .switchPlayer

      case Occupied(_) =>
        copy(message = Some(ErrorMessage.OwnStoneChosen))

      case _ =>
        copy(message = Some(ErrorMessage.InvalidMoveRemove))

  private def boardString: String =
    def Feld(pos: Int): String = board(pos) match
      case Empty => f"$pos%2d"
      case Occupied(Player.White) => " W"
      case Occupied(Player.Black) => " B"

    s"""
      |${Feld(16)}---------${Feld(17)}---------${Feld(18)}
      | |          |          |
      | |  ${Feld(8)}-----${Feld(9)}-----${Feld(10)}   |
      | |   |      |      |   |
      | |   |  ${Feld(0)}-${Feld(1)}-${Feld(2)}   |   |
      | |   |   |     |   |   |
      |${Feld(23)}--${Feld(15)}--${Feld(7)}    ${Feld(3)}--${Feld(11)}--${Feld(19)}
      | |   |   |     |   |   |
      | |   |  ${Feld(6)}-${Feld(5)}-${Feld(4)}   |   |
      | |   |      |      |   |
      | |  ${Feld(14)}-----${Feld(13)}-----${Feld(12)}   |
      | |          |          |
      |${Feld(22)}---------${Feld(21)}---------${Feld(20)}
      |"""


  // Überschreibt toString für den ganzen GameState
  override def toString: String =
    s"""
       |Current Player: $currentPlayer
       |White Stones: $whiteStones (to place: $whiteStonesToPlace)
       |Black Stones: $blackStones (to place: $blackStonesToPlace)
       |${boardString}
       |""".stripMargin
  