package de.htwg.se.muehle.util

import de.htwg.se.muehle.gamestate.GameStateInterface

/**
 * Interface for managing undo/redo operations using the Command Pattern.
 *
 * This interface provides a mechanism to execute commands and maintain
 * a history stack for undo/redo functionality. It works in conjunction
 * with [[Command]] objects that encapsulate game actions.
 *
 * The UndoManager maintains two stacks:
 * - Undo stack: Commands that can be undone
 * - Redo stack: Commands that were undone and can be redone
 *
 * When a new command is executed, the redo stack is cleared.
 *
 * @example
 * {{{
 * val undoManager: UndoManagerInterface = new UndoManager()
 *
 * // Execute a command
 * val command = new GameStateCommand(currentState, action)
 * val newState = undoManager.doStep(command)
 *
 * // Undo the command
 * val previousState = undoManager.undoStep(newState)
 *
 * // Redo the command
 * val restoredState = undoManager.redoStep(previousState)
 * }}}
 *
 * @see [[Command]] for the command interface
 * @see [[GameStateCommand]] for the concrete command implementation
 */
trait UndoManagerInterface:

  /**
   * Executes a command and adds it to the undo history.
   *
   * This method:
   * 1. Calls command.doStep to execute the action
   * 2. Pushes the command onto the undo stack
   * 3. Clears the redo stack (new action invalidates redo history)
   *
   * @param command The Command to execute
   * @return The new GameStateInterface resulting from the command execution
   */
  def doStep(command: Command): GameStateInterface

  /**
   * Undoes the last command, reverting to the previous state.
   *
   * This method:
   * 1. Pops the most recent command from the undo stack
   * 2. Calls command.undoStep to revert the action
   * 3. Pushes the command onto the redo stack
   *
   * If the undo stack is empty, returns the current state unchanged.
   *
   * @param current The current game state
   * @return The previous GameStateInterface, or current if nothing to undo
   */
  def undoStep(current: GameStateInterface): GameStateInterface

  /**
   * Redoes a previously undone command.
   *
   * This method:
   * 1. Pops a command from the redo stack
   * 2. Calls command.redoStep to re-execute the action
   * 3. Pushes the command back onto the undo stack
   *
   * If the redo stack is empty, returns the current state unchanged.
   *
   * @param current The current game state
   * @return The restored GameStateInterface, or current if nothing to redo
   */
  def redoStep(current: GameStateInterface): GameStateInterface

  /**
   * Clears both the undo and redo stacks.
   *
   * This should be called when:
   * - Starting a new game
   * - Loading a saved game
   * - Any situation where history should be discarded
   */
  def clear(): Unit
