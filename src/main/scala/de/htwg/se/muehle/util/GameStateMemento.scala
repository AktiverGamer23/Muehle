// GameStateMemento.scala
package de.htwg.se.muehle.util

import de.htwg.se.muehle.gamestate.GameStateInterface
import de.htwg.se.muehle.board.BoardInterface
import de.htwg.se.muehle.player.Player
import de.htwg.se.muehle.model.Message


class GameStateMemento private[util] (
  private val board: BoardInterface,
  private val currentPlayer: Player,
  private val phase: GameStateInterface.Phase,
  private val whiteStonesToPlace: Int,
  private val blackStonesToPlace: Int,
  private val whiteStones: Int,
  private val blackStones: Int,
  private val message: Option[Message]
):


  def restore(): GameStateInterface =
    de.htwg.se.muehle.gamestate.GameState(
      board = board,
      currentPlayer = currentPlayer,
      phase = phase,
      whiteStonesToPlace = whiteStonesToPlace,
      blackStonesToPlace = blackStonesToPlace,
      whiteStones = whiteStones,
      blackStones = blackStones,
      message = message
    )

object GameStateMemento:

  def create(gameState: GameStateInterface): GameStateMemento =
    new GameStateMemento(
      board = gameState.board,
      currentPlayer = gameState.currentPlayer,
      phase = gameState.phase,
      whiteStonesToPlace = gameState.whiteStonesToPlace,
      blackStonesToPlace = gameState.blackStonesToPlace,
      whiteStones = gameState.whiteStones,
      blackStones = gameState.blackStones,
      message = gameState.message
    )
