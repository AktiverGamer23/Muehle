// GameControllerWithMemento.scala
package de.htwg.se.muehle.controller

import com.google.inject.{Inject, Singleton}
import de.htwg.se.muehle.util.*
import de.htwg.se.muehle.player.Player
import de.htwg.se.muehle.gamestate.GameStateInterface
import de.htwg.se.muehle.state.StateInterface
import de.htwg.se.muehle.ai.BotSelectorInterface
import de.htwg.se.muehle.fileio.FileIOInterface
import scala.util.{Try, Success, Failure}

/**
 * Game Controller using Memento Pattern for undo/redo
 *
 * This is an alternative implementation to GameController that uses
 * the Memento Pattern instead of the Command Pattern for state management.
 *
 * Advantages:
 * - Complete snapshots of game state
 * - Can undo/redo complex operations
 * - Better for debugging (can inspect full state history)
 *
 * Trade-offs:
 * - Uses more memory (stores complete states)
 * - Command Pattern is more lightweight
 */
@Singleton
class GameControllerWithMemento @Inject() (
  private var gameState: GameStateInterface,
  private val state: StateInterface,
  private val botSelector: BotSelectorInterface,
  private val fileIO: FileIOInterface
) extends ControllerInterface:

  private val caretaker = new MementoCaretaker()

  // Save initial state once at creation
  caretaker.saveState(gameState)

  override def handle(pos1: Int, pos2: Int = -1): Unit =
    // Execute the action
    gameState = state.handle(gameState, pos1, pos2)
    // Save state AFTER the action (this is the new state we might want to redo to)
    caretaker.saveState(gameState)
    notifyObservers

    handleBotMove()

  private def handleBotMove(): Unit =
    if gameState.currentPlayer == Player.Black then
      botSelector.calculateMove(gameState).foreach { case (botPos1, botPos2) =>
        handle(botPos1, botPos2)
      }

  override def undo(): Unit =
    caretaker.undo(gameState) match
      case Some(previousState) =>
        gameState = previousState
        notifyObservers
      case None =>
        ()

  override def redo(): Unit =
    caretaker.redo() match
      case Some(nextState) =>
        gameState = nextState
        notifyObservers
      case None =>
        ()

  override def getGameState: GameStateInterface = gameState

  override def save(filePath: String): Try[Unit] =
    fileIO.save(gameState, filePath)

  override def load(filePath: String): Try[Unit] =
    fileIO.load(filePath) match
      case Success(loadedState) =>
        gameState = loadedState
        caretaker.clear()
        caretaker.saveState(gameState)
        notifyObservers
        Success(())
      case Failure(exception) =>
        Failure(exception)

  override def restart(): Unit =
    gameState = de.htwg.se.muehle.gamestate.GameState.create(
      board = de.htwg.se.muehle.board.Board.empty,
      whiteStonesToPlace = 9,
      blackStonesToPlace = 9,
      whiteStones = 9,
      blackStones = 9
    )
    caretaker.clear()
    caretaker.saveState(gameState)
    notifyObservers



  def canUndo: Boolean = caretaker.canUndo

  def canRedo: Boolean = caretaker.canRedo

  def undoCount: Int = caretaker.undoCount

  def redoCount: Int = caretaker.redoCount
