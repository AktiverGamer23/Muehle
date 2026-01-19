// GameStateInterface.scala
package de.htwg.se.muehle.gamestate

import de.htwg.se.muehle.board.BoardInterface
import de.htwg.se.muehle.player.Player
import de.htwg.se.muehle.model.*

trait GameStateInterface:
  def board: BoardInterface
  def currentPlayer: Player
  def phase: GameStateInterface.Phase
  def whiteStonesToPlace: Int
  def blackStonesToPlace: Int
  def whiteStones: Int
  def blackStones: Int
  def message: Option[Message]
  def getPlayer(pos: Int): Option[Player]
  def isPlacingPhaseOver: Boolean

object GameStateInterface:
  sealed trait Phase
  case object PlacingPhase extends Phase
  case object MovingPhase extends Phase
  case object MillRemovePhase extends Phase