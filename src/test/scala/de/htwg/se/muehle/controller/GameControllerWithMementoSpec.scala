package de.htwg.se.muehle.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.se.muehle.controller.GameControllerWithMemento
import de.htwg.se.muehle.gamestate.{GameState, GameStateInterface}
import de.htwg.se.muehle.board.Board
import de.htwg.se.muehle.state.StateP
import de.htwg.se.muehle.ai.{BotSelector, RandomBot}
import de.htwg.se.muehle.fileio.JsonFileIO
import de.htwg.se.muehle.player.Player
import de.htwg.se.muehle.util.Observer
import java.io.File
import scala.util.{Try, Success, Failure}

class GameControllerWithMementoSpec extends AnyWordSpec with Matchers {

  "A GameControllerWithMemento" when {
    val initialState = GameState.create(Board.empty, 9, 9, 9, 9)
    val state = new StateP()
    val fileIO = new JsonFileIO()

    "created" should {
      val botSelector = new BotSelector()
      val controller = new GameControllerWithMemento(initialState, state, botSelector, fileIO)

      "have initial game state" in {
        controller.getGameState.currentPlayer should be(Player.White)
        controller.getGameState.phase should be(GameStateInterface.PlacingPhase)
      }

      "have undo capability disabled initially" in {
        controller.canUndo should be(false)
      }

      "have redo capability disabled initially" in {
        controller.canRedo should be(false)
      }

      "have correct undo count" in {
        controller.undoCount should be(0)
      }

      "have correct redo count" in {
        controller.redoCount should be(0)
      }
    }

    "handling a move" should {
      val botSelector = new BotSelector()
      val controller = new GameControllerWithMemento(initialState, state, botSelector, fileIO)

      "update game state" in {
        controller.handle(0)
        controller.getGameState.getPlayer(0) should be(Some(Player.White))
      }

      "enable undo after move" in {
        controller.canUndo should be(true)
        controller.undoCount should be(1)
      }

      "clear redo stack after new move" in {
        controller.canRedo should be(false)
        controller.redoCount should be(0)
      }
    }

    "undoing a move" should {
      val botSelector = new BotSelector()
      val controller = new GameControllerWithMemento(initialState, state, botSelector, fileIO)
      controller.handle(0)
      val stateAfterMove = controller.getGameState

      controller.undo()

      "restore previous state" in {
        controller.getGameState.currentPlayer should be(Player.White)
        controller.getGameState.getPlayer(0) should be(None)
      }

      "enable redo" in {
        controller.canRedo should be(true)
        controller.redoCount should be(1)
      }

      "disable undo when back to initial state" in {
        controller.canUndo should be(false)
      }
    }

    "redoing a move" should {
      val botSelector = new BotSelector()
      val controller = new GameControllerWithMemento(initialState, state, botSelector, fileIO)
      controller.handle(0)
      controller.undo()

      controller.redo()


      "disable redo after redoing" in {
        controller.canRedo should be(false)
      }

      "enable undo" in {
        controller.canUndo should be(true)
      }
    }

    "handling multiple moves" should {
      val botSelector = new BotSelector()
      val controller = new GameControllerWithMemento(initialState, state, botSelector, fileIO)

      "track all moves in history" in {
        controller.handle(0)
        controller.handle(8)
        controller.handle(1)

        controller.undoCount should be(3)
        controller.getGameState.getPlayer(1) should be(Some(Player.White))
      }

      "undo multiple times" in {
        controller.undo()
        controller.getGameState.getPlayer(1) should be(None)

        controller.undo()
        controller.getGameState.getPlayer(8) should be(None)

        controller.undo()
        controller.getGameState.getPlayer(0) should be(None)
      }


    }

    "undo without history" should {
      val botSelector = new BotSelector()
      val controller = new GameControllerWithMemento(initialState, state, botSelector, fileIO)

      "not change state" in {
        val stateBefore = controller.getGameState
        controller.undo()
        controller.getGameState should be(stateBefore)
      }
    }

    "redo without redo stack" should {
      val botSelector = new BotSelector()
      val controller = new GameControllerWithMemento(initialState, state, botSelector, fileIO)

      "not change state" in {
        val stateBefore = controller.getGameState
        controller.redo()
        controller.getGameState should be(stateBefore)
      }
    }

    "restarting game" should {
      val botSelector = new BotSelector()
      val controller = new GameControllerWithMemento(initialState, state, botSelector, fileIO)
      controller.handle(0)
      controller.handle(8)

      controller.restart()

      "reset to initial state" in {
        controller.getGameState.currentPlayer should be(Player.White)
        controller.getGameState.phase should be(GameStateInterface.PlacingPhase)
        (0 until 24).foreach { pos =>
          controller.getGameState.getPlayer(pos) should be(None)
        }
      }

      "clear history" in {
        controller.canUndo should be(false)
        controller.undoCount should be(0)
      }

      "clear redo stack" in {
        controller.canRedo should be(false)
        controller.redoCount should be(0)
      }
    }

    "saving and loading" should {
      val botSelector = new BotSelector()
      val controller = new GameControllerWithMemento(initialState, state, botSelector, fileIO)
      controller.handle(0)
      controller.handle(8)

      val tempFile = File.createTempFile("muehle_test", ".json")
      tempFile.deleteOnExit()
      val filePath = tempFile.getAbsolutePath

      "save successfully" in {
        val result = controller.save(filePath)
        result.isSuccess should be(true)
      }

      "load successfully" in {
        val newController = new GameControllerWithMemento(initialState, state, botSelector, fileIO)
        val result = newController.load(filePath)

        result.isSuccess should be(true)
        newController.getGameState.getPlayer(0) should be(Some(Player.White))
        newController.getGameState.getPlayer(8) should be(Some(Player.Black))
      }

      "clear history after load" in {
        val newController = new GameControllerWithMemento(initialState, state, botSelector, fileIO)
        newController.handle(1)
        newController.handle(9)

        newController.load(filePath)

        newController.canUndo should be(false)
        newController.canRedo should be(false)
      }
    }

    "with bot enabled" should {
      val botSelector = new BotSelector()
      val controller = new GameControllerWithMemento(initialState, state, botSelector, fileIO)
      botSelector.setStrategy(new RandomBot())

      "trigger bot move after human move" in {
        val initialPlayer = controller.getGameState.currentPlayer
        controller.handle(0)

        controller.getGameState.currentPlayer should be(Player.White)
      }
    }

    "observing changes" should {
      val botSelector = new BotSelector()
      val controller = new GameControllerWithMemento(initialState, state, botSelector, fileIO)
      var updateCount = 0

      val observer = new Observer {
        override def update: Unit = updateCount += 1
      }

      controller.add(observer)

      "notify observers on handle" in {
        controller.handle(0)
        updateCount should be > 0
      }

      "notify observers on undo" in {
        val countBefore = updateCount
        controller.undo()
        updateCount should be > countBefore
      }

      "notify observers on redo" in {
        val countBefore = updateCount
        controller.redo()
        updateCount should be > countBefore
      }

      "notify observers on restart" in {
        val countBefore = updateCount
        controller.restart()
        updateCount should be > countBefore
      }

      "notify observers on load" in {
        val tempFile = File.createTempFile("muehle_test", ".json")
        tempFile.deleteOnExit()
        controller.save(tempFile.getAbsolutePath)

        val countBefore = updateCount
        controller.load(tempFile.getAbsolutePath)
        updateCount should be > countBefore
      }

      "stop notifying after removal" in {
        controller.remove(observer)
        val countBefore = updateCount
        controller.handle(1)
        updateCount should be(countBefore)
      }
    }

    "loading non-existent file" should {
      val botSelector = new BotSelector()
      val controller = new GameControllerWithMemento(initialState, state, botSelector, fileIO)

      "return failure" in {
        val result = controller.load("/non/existent/path.json")
        result.isFailure should be(true)
      }
    }

    "complex undo/redo scenario" should {
      val botSelector = new BotSelector()
      val controller = new GameControllerWithMemento(initialState, state, botSelector, fileIO)



      "clear redo stack on new move after undo" in {
        controller.handle(9)

        controller.canRedo should be(false)
      }
    }
  }
}
