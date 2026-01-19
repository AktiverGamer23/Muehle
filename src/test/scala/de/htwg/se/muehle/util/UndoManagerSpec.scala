package de.htwg.se.muehle.util

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.se.muehle.gamestate.{GameState, GameStateInterface}
import de.htwg.se.muehle.board.Board
import de.htwg.se.muehle.player.Player

class UndoManagerSpec extends AnyWordSpec with Matchers {

  "An UndoManager" when {
    val initialState = GameState.create(Board.empty, 9, 9, 9, 9)

    "created" should {
      val manager = new UndoManager

      "have empty undo stack" in {
        val result = manager.undoStep(initialState)
        result should be(initialState)
      }

      "have empty redo stack" in {
        val result = manager.redoStep(initialState)
        result should be(initialState)
      }
    }

    "performing a command" should {
      val manager = new UndoManager
      val action: GameStateInterface => GameStateInterface = { gs =>
        gs.asInstanceOf[GameState].copy(
          board = gs.board.set(0, Some(Player.White)),
          whiteStonesToPlace = gs.whiteStonesToPlace - 1
        )
      }
      val command = new GameStateCommand(initialState, action)

      "execute the command and return new state" in {
        val newState = manager.doStep(command)
        newState.getPlayer(0) should be(Some(Player.White))
        newState.whiteStonesToPlace should be(8)
      }

      "add command to undo stack" in {
        val currentState = manager.doStep(command)
        val undoneState = manager.undoStep(currentState)
        undoneState.getPlayer(0) should be(None)
        undoneState.whiteStonesToPlace should be(9)
      }

      "clear redo stack" in {
        val state1 = manager.doStep(command)
        manager.undoStep(state1)

        val action2: GameStateInterface => GameStateInterface = { gs =>
          gs.asInstanceOf[GameState].copy(currentPlayer = Player.Black)
        }
        val command2 = new GameStateCommand(initialState, action2)
        manager.doStep(command2)

        val result = manager.redoStep(initialState)
        result should be(initialState)
      }
    }

    "undoing a command" should {
      val manager = new UndoManager
      val action: GameStateInterface => GameStateInterface = { gs =>
        gs.asInstanceOf[GameState].copy(currentPlayer = Player.Black)
      }
      val command = new GameStateCommand(initialState, action)

      val newState = manager.doStep(command)

      "restore previous state" in {
        val undoneState = manager.undoStep(newState)
        undoneState.currentPlayer should be(Player.White)
      }

      "add command to redo stack" in {
        val undoneState = manager.undoStep(newState)
        val redoneState = manager.redoStep(undoneState)
        redoneState.currentPlayer should be(Player.Black)
      }

      "remove command from undo stack" in {
        manager.undoStep(newState)
        val secondUndo = manager.undoStep(initialState)
        secondUndo should be(initialState)
      }
    }

    "redoing a command" should {
      val manager = new UndoManager
      val action: GameStateInterface => GameStateInterface = { gs =>
        gs.asInstanceOf[GameState].copy(
          board = gs.board.set(5, Some(Player.White))
        )
      }
      val command = new GameStateCommand(initialState, action)

      val newState = manager.doStep(command)
      val undoneState = manager.undoStep(newState)

      "restore next state" in {
        val redoneState = manager.redoStep(undoneState)
        redoneState.getPlayer(5) should be(Some(Player.White))
      }

      "add command back to undo stack" in {
        val redoneState = manager.redoStep(undoneState)
        val undoneAgain = manager.undoStep(redoneState)
        undoneAgain.getPlayer(5) should be(None)
      }

      "remove command from redo stack" in {
        manager.redoStep(undoneState)
        val secondRedo = manager.redoStep(initialState)
        secondRedo should be(initialState)
      }
    }

    "with multiple commands" should {
      val manager = new UndoManager

      val command1 = new GameStateCommand(initialState, { gs =>
        gs.asInstanceOf[GameState].copy(board = gs.board.set(0, Some(Player.White)))
      })

      val state1 = manager.doStep(command1)

      val command2 = new GameStateCommand(state1, { gs =>
        gs.asInstanceOf[GameState].copy(board = gs.board.set(8, Some(Player.Black)))
      })

      val state2 = manager.doStep(command2)

      val command3 = new GameStateCommand(state2, { gs =>
        gs.asInstanceOf[GameState].copy(board = gs.board.set(1, Some(Player.White)))
      })

      val state3 = manager.doStep(command3)

      "undo multiple times" in {
        val undo1 = manager.undoStep(state3)
        undo1.getPlayer(1) should be(None)
        undo1.getPlayer(0) should be(Some(Player.White))
        undo1.getPlayer(8) should be(Some(Player.Black))

        val undo2 = manager.undoStep(undo1)
        undo2.getPlayer(8) should be(None)
        undo2.getPlayer(0) should be(Some(Player.White))

        val undo3 = manager.undoStep(undo2)
        undo3.getPlayer(0) should be(None)
      }

      "redo multiple times" in {
        manager.undoStep(state3)
        manager.undoStep(state2)
        manager.undoStep(state1)

        val redo1 = manager.redoStep(initialState)
        redo1.getPlayer(0) should be(Some(Player.White))

        val redo2 = manager.redoStep(redo1)
        redo2.getPlayer(8) should be(Some(Player.Black))

        val redo3 = manager.redoStep(redo2)
        redo3.getPlayer(1) should be(Some(Player.White))
      }

      "handle interleaved undo/redo" in {
        val newManager = new UndoManager
        val s1 = newManager.doStep(command1)
        val s2 = newManager.doStep(new GameStateCommand(s1, { gs =>
          gs.asInstanceOf[GameState].copy(board = gs.board.set(8, Some(Player.Black)))
        }))

        val undo1 = newManager.undoStep(s2)
        val redo1 = newManager.redoStep(undo1)

        redo1.getPlayer(0) should be(Some(Player.White))
        redo1.getPlayer(8) should be(Some(Player.Black))
      }
    }

    "clearing" should {
      val manager = new UndoManager
      val action: GameStateInterface => GameStateInterface = { gs =>
        gs.asInstanceOf[GameState].copy(currentPlayer = Player.Black)
      }
      val command = new GameStateCommand(initialState, action)

      manager.doStep(command)
      manager.undoStep(initialState)

      "clear both stacks" in {
        manager.clear()

        val undoResult = manager.undoStep(initialState)
        undoResult should be(initialState)

        val redoResult = manager.redoStep(initialState)
        redoResult should be(initialState)
      }
    }

    "undo without history" should {
      val manager = new UndoManager

      "return current state" in {
        val result = manager.undoStep(initialState)
        result should be(initialState)
      }
    }

    "redo without redo stack" should {
      val manager = new UndoManager

      "return current state" in {
        val result = manager.redoStep(initialState)
        result should be(initialState)
      }
    }

    "complex scenario" should {
      "handle full workflow" in {
        val manager = new UndoManager

        val cmd1 = new GameStateCommand(initialState, { gs =>
          gs.asInstanceOf[GameState].copy(board = gs.board.set(0, Some(Player.White)))
        })
        val s1 = manager.doStep(cmd1)

        val cmd2 = new GameStateCommand(s1, { gs =>
          gs.asInstanceOf[GameState].copy(board = gs.board.set(8, Some(Player.Black)))
        })
        val s2 = manager.doStep(cmd2)

        val undo1 = manager.undoStep(s2)
        undo1.getPlayer(8) should be(None)

        val cmd3 = new GameStateCommand(undo1, { gs =>
          gs.asInstanceOf[GameState].copy(board = gs.board.set(1, Some(Player.White)))
        })
        val s3 = manager.doStep(cmd3)

        val redoAfterNewCommand = manager.redoStep(s3)
        redoAfterNewCommand should be(s3)
      }
    }

    "UndoManagerInterface implementation" should {
      val manager: UndoManagerInterface = new UndoManager
      val action: GameStateInterface => GameStateInterface = identity
      val command = new GameStateCommand(initialState, action)

      "implement doStep" in {
        val result = manager.doStep(command)
        result.isInstanceOf[GameStateInterface] should be(true)
      }

      "implement undoStep" in {
        val result = manager.undoStep(initialState)
        result.isInstanceOf[GameStateInterface] should be(true)
      }

      "implement redoStep" in {
        val result = manager.redoStep(initialState)
        result.isInstanceOf[GameStateInterface] should be(true)
      }

      "implement clear" in {
        manager.clear()
      }
    }
  }
}
