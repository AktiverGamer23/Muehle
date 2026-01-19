// GameStateInterface.scala
package de.htwg.se.muehle.gamestate

import de.htwg.se.muehle.board.BoardInterface
import de.htwg.se.muehle.player.Player
import de.htwg.se.muehle.model.*

/**
 * Interface representing the complete state of a Muehle game.
 *
 * This interface encapsulates all information needed to represent a game at any point:
 * - The current board configuration
 * - Which player's turn it is
 * - The current game phase
 * - Stone counts for both players
 * - Any messages (e.g., winner announcements)
 *
 * Implementations should be immutable to support undo/redo operations.
 *
 * @see [[GameStateInterface.Phase]] for possible game phases
 * @see [[de.htwg.se.muehle.board.BoardInterface]] for board operations
 */
trait GameStateInterface:

  /**
   * The current board configuration.
   * @return The BoardInterface representing the current stone positions
   */
  def board: BoardInterface

  /**
   * The player whose turn it currently is.
   * @return Player.White or Player.Black
   */
  def currentPlayer: Player

  /**
   * The current phase of the game.
   * @return One of PlacingPhase, MovingPhase, or MillRemovePhase
   */
  def phase: GameStateInterface.Phase

  /**
   * Number of white stones still to be placed on the board.
   * @return Count of remaining white stones (0-9)
   */
  def whiteStonesToPlace: Int

  /**
   * Number of black stones still to be placed on the board.
   * @return Count of remaining black stones (0-9)
   */
  def blackStonesToPlace: Int

  /**
   * Total number of white stones in play (on board + to place).
   * @return Count of white stones (0-9)
   */
  def whiteStones: Int

  /**
   * Total number of black stones in play (on board + to place).
   * @return Count of black stones (0-9)
   */
  def blackStones: Int

  /**
   * Optional message about the current game state.
   *
   * Used to communicate events like:
   * - Stone placement/movement instructions
   * - Mill formation (stone removal required)
   * - Winner announcement
   *
   * @return Some(Message) if there's a message, None otherwise
   */
  def message: Option[Message]

  /**
   * Gets the player who has a stone at the specified position.
   *
   * Convenience method that delegates to board.stoneAt.
   *
   * @param pos The position to check (0-23)
   * @return Some(Player) if occupied, None if empty
   */
  def getPlayer(pos: Int): Option[Player]

  /**
   * Checks if the placing phase is complete for both players.
   *
   * @return true if both players have placed all their stones
   */
  def isPlacingPhaseOver: Boolean

/**
 * Companion object containing game phase definitions.
 */
object GameStateInterface:

  /**
   * Sealed trait representing the possible phases of a Muehle game.
   */
  sealed trait Phase

  /**
   * Initial phase where players take turns placing stones on the board.
   * Each player places 9 stones. A stone can be placed on any empty position.
   */
  case object PlacingPhase extends Phase

  /**
   * Main phase where players move their stones to adjacent positions.
   * A player with only 3 stones remaining may "fly" (move to any empty position).
   */
  case object MovingPhase extends Phase

  /**
   * Special phase entered when a player forms a mill (3 in a row).
   * The player must remove one opponent's stone that is not part of a mill.
   */
  case object MillRemovePhase extends Phase