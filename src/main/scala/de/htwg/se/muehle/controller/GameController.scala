// GameController.scala - Command Pattern Implementation
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
 * Game Controller using Command Pattern for undo/redo
 *
 * This implementation uses the Command Pattern for state management.
 * Each action is encapsulated in a Command object that knows how to
 * execute and reverse itself.
 *
 * Advantages:
 * - Lightweight (stores only deltas/commands)
 * - Good for simple, reversible operations
 * - Lower memory usage than Memento
 *
 * Trade-offs:
 * - Commands must explicitly know how to undo themselves
 * - Complex operations may be harder to reverse
 */
@Singleton
class GameController @Inject() (
  private var gameState: GameStateInterface,
  private val state: StateInterface,
  private val botSelector: BotSelectorInterface,
  private val fileIO: FileIOInterface
) extends ControllerInterface:

  private val undoManager = new UndoManager()

  override def handle(pos1: Int, pos2: Int = -1): Unit =
    val command = new GameStateCommand(
      gameState,
      currentState => state.handle(currentState, pos1, pos2)
    )
    gameState = undoManager.doStep(command)
    notifyObservers

    handleBotMove()

  private def handleBotMove(): Unit =
    if gameState.currentPlayer == Player.Black then
      botSelector.calculateMove(gameState).foreach { case (botPos1, botPos2) =>
        handle(botPos1, botPos2)
      }

  override def undo(): Unit =
    gameState = undoManager.undoStep(gameState)
    notifyObservers

  override def redo(): Unit =
    gameState = undoManager.redoStep(gameState)
    notifyObservers

  override def getGameState: GameStateInterface = gameState

  override def save(filePath: String): Try[Unit] =
    fileIO.save(gameState, filePath)

  override def load(filePath: String): Try[Unit] =
    fileIO.load(filePath) match
      case Success(loadedState) =>
        gameState = loadedState
        undoManager.clear()
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
    undoManager.clear()
    notifyObservers
