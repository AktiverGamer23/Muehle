// MementoCaretaker.scala
package de.htwg.se.muehle.util

import de.htwg.se.muehle.gamestate.GameStateInterface


class MementoCaretaker:

  private var history: List[GameStateMemento] = Nil
  private var redoStack: List[GameStateMemento] = Nil


  def saveState(gameState: GameStateInterface): Unit =
    val memento = GameStateMemento.create(gameState)
    history = memento :: history
    redoStack = Nil // Clear redo stack when a new action is performed

  def undo(currentState: GameStateInterface): Option[GameStateInterface] =
    history match
      case Nil => None
      case current :: Nil => None 
      case current :: previous :: rest =>
        redoStack = current :: redoStack
        history = previous :: rest
        Some(previous.restore())

  /**
   * Restores the next game state from redo stack
   * Returns None if there's nothing to redo
   */
  def redo(): Option[GameStateInterface] =
    redoStack match
      case Nil => None
      case next :: rest =>
        history = next :: history
        redoStack = rest
        Some(next.restore())

  /**
   * Clears all history and redo stack
   */
  def clear(): Unit =
    history = Nil
    redoStack = Nil

  /**
   * Returns the number of states that can be undone
   */
  def undoCount: Int = math.max(0, history.length - 1)

  /**
   * Returns the number of states that can be redone
   */
  def redoCount: Int = redoStack.length

  /**
   * Checks if undo is possible
   */
  def canUndo: Boolean = history.length > 1

  /**
   * Checks if redo is possible
   */
  def canRedo: Boolean = redoStack.nonEmpty
