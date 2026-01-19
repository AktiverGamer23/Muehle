// GameState.scala
package de.htwg.se.muehle.gamestate

import de.htwg.se.muehle.board.BoardInterface
import de.htwg.se.muehle.player.Player
import de.htwg.se.muehle.model.*

case class GameState(
  board: BoardInterface,
  currentPlayer: Player,
  phase: GameStateInterface.Phase,
  whiteStonesToPlace: Int,
  blackStonesToPlace: Int,
  whiteStones: Int,
  blackStones: Int,
  message: Option[Message]
) extends GameStateInterface:

  override def getPlayer(pos: Int): Option[Player] =
    board.stoneAt(pos)

  override def isPlacingPhaseOver: Boolean =
    whiteStonesToPlace == 0 && blackStonesToPlace == 0

object GameState:
  def create(
    board: BoardInterface,
    whiteStonesToPlace: Int,
    blackStonesToPlace: Int,
    whiteStones: Int,
    blackStones: Int
  ): GameStateInterface =
    new GameState(
      board = board,
      currentPlayer = Player.White,
      phase = GameStateInterface.PlacingPhase,
      whiteStonesToPlace = whiteStonesToPlace,
      blackStonesToPlace = blackStonesToPlace,
      whiteStones = whiteStones,
      blackStones = blackStones,
      message = Some(PlaceStoneMessage.Success)
    )