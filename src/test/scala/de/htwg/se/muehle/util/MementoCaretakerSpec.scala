package de.htwg.se.muehle.util

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.se.muehle.gamestate.{GameState, GameStateInterface}
import de.htwg.se.muehle.board.Board
import de.htwg.se.muehle.player.Player

class MementoCaretakerSpec extends AnyWordSpec with Matchers {

  "A MementoCaretaker" when {
    val initialState = GameState.create(Board.empty, 9, 9, 9, 9)

    "created" should {
      val caretaker = new MementoCaretaker

      "have empty history" in {
        caretaker.undoCount should be(0)
      }

      "have empty redo stack" in {
        caretaker.redoCount should be(0)
      }

      "not allow undo" in {
        caretaker.canUndo should be(false)
      }

      "not allow redo" in {
        caretaker.canRedo should be(false)
      }
    }

    "saving state" should {
      val caretaker = new MementoCaretaker

      "save initial state" in {
        caretaker.saveState(initialState)
        caretaker.undoCount should be(0)
      }

      "save multiple states" in {
        val state2 = initialState.asInstanceOf[GameState].copy(
          board = initialState.board.set(0, Some(Player.White))
        )
        caretaker.saveState(state2)

        caretaker.undoCount should be(1)
        caretaker.canUndo should be(true)
      }

      "clear redo stack on new save" in {
        val state3 = initialState.asInstanceOf[GameState].copy(
          board = initialState.board.set(8, Some(Player.Black))
        )
        caretaker.saveState(state3)

        caretaker.undo(state3)

        val state4 = state3.asInstanceOf[GameState].copy(
          board = state3.board.set(1, Some(Player.White))
        )
        caretaker.saveState(state4)

        caretaker.canRedo should be(false)
        caretaker.redoCount should be(0)
      }
    }

    "undoing" should {
      val caretaker = new MementoCaretaker
      caretaker.saveState(initialState)

      val state2 = initialState.asInstanceOf[GameState].copy(
        board = initialState.board.set(0, Some(Player.White)),
        whiteStonesToPlace = 8
      )
      caretaker.saveState(state2)

      "restore previous state" in {
        val undone = caretaker.undo(state2)
        undone should be(defined)
        undone.get.getPlayer(0) should be(None)
        undone.get.whiteStonesToPlace should be(9)
      }

      "update undo count" in {
        caretaker.undoCount should be(0)
      }

      "update redo count" in {
        caretaker.redoCount should be(1)
        caretaker.canRedo should be(true)
      }

      "return None when only one state exists" in {
        val singleStateCaretaker = new MementoCaretaker
        singleStateCaretaker.saveState(initialState)

        val result = singleStateCaretaker.undo(initialState)
        result should be(None)
      }

      "return None with empty history" in {
        val emptyCaretaker = new MementoCaretaker
        val result = emptyCaretaker.undo(initialState)
        result should be(None)
      }
    }

    "redoing" should {
      val caretaker = new MementoCaretaker
      caretaker.saveState(initialState)

      val state2 = initialState.asInstanceOf[GameState].copy(
        board = initialState.board.set(5, Some(Player.Black))
      )
      caretaker.saveState(state2)

      caretaker.undo(state2)

      "restore next state" in {
        val redone = caretaker.redo()
        redone should be(defined)
        redone.get.getPlayer(5) should be(Some(Player.Black))
      }

      "update redo count" in {
        caretaker.redoCount should be(0)
        caretaker.canRedo should be(false)
      }

      "update undo count" in {
        caretaker.undoCount should be(1)
        caretaker.canUndo should be(true)
      }

      "return None with empty redo stack" in {
        val result = caretaker.redo()
        result should be(None)
      }
    }

    "with multiple states" should {
      val caretaker = new MementoCaretaker

      val state1 = initialState
      caretaker.saveState(state1)

      val state2 = state1.asInstanceOf[GameState].copy(
        board = state1.board.set(0, Some(Player.White))
      )
      caretaker.saveState(state2)

      val state3 = state2.asInstanceOf[GameState].copy(
        board = state2.board.set(8, Some(Player.Black))
      )
      caretaker.saveState(state3)

      val state4 = state3.asInstanceOf[GameState].copy(
        board = state3.board.set(1, Some(Player.White))
      )
      caretaker.saveState(state4)

      "undo multiple times" in {
        caretaker.undoCount should be(3)

        val undo1 = caretaker.undo(state4).get
        undo1.getPlayer(1) should be(None)
        caretaker.undoCount should be(2)

        val undo2 = caretaker.undo(undo1).get
        undo2.getPlayer(8) should be(None)
        caretaker.undoCount should be(1)

        val undo3 = caretaker.undo(undo2).get
        undo3.getPlayer(0) should be(None)
        caretaker.undoCount should be(0)
      }

      "redo multiple times" in {
        val redo1 = caretaker.redo().get
        redo1.getPlayer(0) should be(Some(Player.White))
        caretaker.redoCount should be(2)

        val redo2 = caretaker.redo().get
        redo2.getPlayer(8) should be(Some(Player.Black))
        caretaker.redoCount should be(1)

        val redo3 = caretaker.redo().get
        redo3.getPlayer(1) should be(Some(Player.White))
        caretaker.redoCount should be(0)
      }
    }

    "clearing" should {
      val caretaker = new MementoCaretaker
      caretaker.saveState(initialState)

      val state2 = initialState.asInstanceOf[GameState].copy(
        board = initialState.board.set(0, Some(Player.White))
      )
      caretaker.saveState(state2)

      caretaker.undo(state2)

      "clear all history" in {
        caretaker.clear()

        caretaker.undoCount should be(0)
        caretaker.redoCount should be(0)
        caretaker.canUndo should be(false)
        caretaker.canRedo should be(false)
      }

      "prevent undo after clear" in {
        val result = caretaker.undo(state2)
        result should be(None)
      }

      "prevent redo after clear" in {
        val result = caretaker.redo()
        result should be(None)
      }
    }

    "complex scenario" should {
      "handle interleaved undo/redo" in {
        val caretaker = new MementoCaretaker

        val s1 = initialState
        caretaker.saveState(s1)

        val s2 = s1.asInstanceOf[GameState].copy(
          board = s1.board.set(0, Some(Player.White))
        )
        caretaker.saveState(s2)

        val s3 = s2.asInstanceOf[GameState].copy(
          board = s2.board.set(8, Some(Player.Black))
        )
        caretaker.saveState(s3)

        val undo1 = caretaker.undo(s3).get
        undo1.getPlayer(8) should be(None)

        val undo2 = caretaker.undo(undo1).get
        undo2.getPlayer(0) should be(None)

        val redo1 = caretaker.redo().get
        redo1.getPlayer(0) should be(Some(Player.White))

        val s4 = redo1.asInstanceOf[GameState].copy(
          board = redo1.board.set(1, Some(Player.White))
        )
        caretaker.saveState(s4)

        caretaker.canRedo should be(false)
        caretaker.undoCount should be(2)
      }
    }

    "with GameStateMemento" should {
      "preserve complete state" in {
        val caretaker = new MementoCaretaker

        val complexState = GameState(
          board = Board.empty
            .set(0, Some(Player.White))
            .set(8, Some(Player.Black))
            .set(16, Some(Player.White)),
          currentPlayer = Player.Black,
          phase = GameStateInterface.MovingPhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 7,
          blackStones = 6,
          message = Some(de.htwg.se.muehle.model.MoveStoneMessage.Success)
        )

        caretaker.saveState(initialState)
        caretaker.saveState(complexState)

        val undone = caretaker.undo(complexState).get

        undone.currentPlayer should be(initialState.currentPlayer)
        undone.phase should be(initialState.phase)
        undone.whiteStonesToPlace should be(initialState.whiteStonesToPlace)
        undone.blackStonesToPlace should be(initialState.blackStonesToPlace)
        undone.whiteStones should be(initialState.whiteStones)
        undone.blackStones should be(initialState.blackStones)
      }
    }

    "canUndo and canRedo flags" should {
      "reflect state correctly" in {
        val caretaker = new MementoCaretaker

        caretaker.canUndo should be(false)
        caretaker.canRedo should be(false)

        caretaker.saveState(initialState)
        caretaker.canUndo should be(false)

        val state2 = initialState.asInstanceOf[GameState].copy(
          board = initialState.board.set(0, Some(Player.White))
        )
        caretaker.saveState(state2)
        caretaker.canUndo should be(true)
        caretaker.canRedo should be(false)

        caretaker.undo(state2)
        caretaker.canUndo should be(false)
        caretaker.canRedo should be(true)

        caretaker.redo()
        caretaker.canUndo should be(true)
        caretaker.canRedo should be(false)
      }
    }
  }
}
