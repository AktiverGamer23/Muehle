package de.htwg.se.muehle.controller

import de.htwg.se.muehle.util.Observable
import de.htwg.se.muehle.gamestate.GameStateInterface
import scala.util.Try

/**
 * Controller interface for the Muehle (Nine Men's Morris) game.
 *
 * This interface defines the main entry point for game logic operations.
 * It follows the MVC pattern, acting as the intermediary between the view
 * and the model. Implementations handle user input, manage game state,
 * and notify observers of changes.
 *
 * @see [[de.htwg.se.muehle.util.Observable]] for the observer pattern implementation
 * @see [[de.htwg.se.muehle.gamestate.GameStateInterface]] for game state details
 */
trait ControllerInterface extends Observable:

  /**
   * Handles a player action at the specified position(s).
   *
   * The behavior depends on the current game phase:
   * - '''PlacingPhase''': Places a stone at pos1 (pos2 is ignored)
   * - '''MovingPhase''': Moves a stone from pos1 to pos2
   * - '''MillRemovePhase''': Removes opponent's stone at pos1 (pos2 is ignored)
   *
   * @param pos1 The primary position (0-23) for the action
   * @param pos2 The secondary position for move actions, default is -1 (unused)
   */
  def handle(pos1: Int, pos2: Int = -1): Unit

  /**
   * Undoes the last action, reverting to the previous game state.
   *
   * If no actions can be undone (e.g., at the initial state), this method
   * has no effect. Notifies observers after state change.
   */
  def undo(): Unit

  /**
   * Redoes a previously undone action.
   *
   * If no actions can be redone, this method has no effect.
   * Notifies observers after state change.
   */
  def redo(): Unit

  /**
   * Returns the current game state.
   *
   * @return The current [[GameStateInterface]] containing board, players, and phase information
   */
  def getGameState: GameStateInterface

  /**
   * Saves the current game state to a file.
   *
   * The file format (XML or JSON) depends on the configured FileIO implementation.
   *
   * @param filePath The path where the game should be saved
   * @return Success if saved successfully, Failure with exception otherwise
   */
  def save(filePath: String): Try[Unit]

  /**
   * Loads a game state from a file.
   *
   * Replaces the current game state with the loaded one and clears undo/redo history.
   * Notifies observers after loading.
   *
   * @param filePath The path to the save file
   * @return Success if loaded successfully, Failure with exception otherwise
   */
  def load(filePath: String): Try[Unit]

  /**
   * Restarts the game with a fresh initial state.
   *
   * Resets the board to empty, restores all stones, and clears undo/redo history.
   * Notifies observers after restart.
   */
  def restart(): Unit
