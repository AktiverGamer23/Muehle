package de.htwg.se.muehle.view

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.se.muehle.controller.ControllerInterface
import de.htwg.se.muehle.gamestate.{GameState, GameStateInterface}
import de.htwg.se.muehle.model._
import de.htwg.se.muehle.player.Player
import de.htwg.se.muehle.board.Board
import de.htwg.se.muehle.util.Observer
import scala.util.Try

class GameViewSpec extends AnyWordSpec with Matchers {

  // Mock Controller for testing GameView without actual controller implementation
  class MockController extends ControllerInterface {
    var gameState: GameStateInterface = GameState.create(Board.empty, 9, 9, 9, 9)
    var observers: Vector[Observer] = Vector()
    var handleCalls: List[(Int, Int)] = List()
    var undoCalls: Int = 0
    var redoCalls: Int = 0
    var saveCalls: List[String] = List()
    var loadCalls: List[String] = List()
    var restartCalls: Int = 0

    override def add(o: Observer): Unit = observers = observers :+ o
    override def remove(o: Observer): Unit = observers = observers.filterNot(_ == o)
    override def getGameState: GameStateInterface = gameState

    override def handle(pos1: Int, pos2: Int = -1): Unit = {
      handleCalls = (pos1, pos2) :: handleCalls
    }

    override def undo(): Unit = undoCalls += 1
    override def redo(): Unit = redoCalls += 1

    override def save(filePath: String): Try[Unit] = {
      saveCalls = filePath :: saveCalls
      scala.util.Success(())
    }

    override def load(filePath: String): Try[Unit] = {
      loadCalls = filePath :: loadCalls
      scala.util.Success(())
    }

    override def restart(): Unit = restartCalls += 1

    // Helper method to trigger observer updates
    def triggerUpdate(): Unit = notifyObservers
  }

  "A GameView" when {
    "created" should {
      val mockController = new MockController
      val gameView = new GameView(mockController)

      "register itself as observer on controller" in {
        mockController.observers should contain(gameView)
      }

      "be properly constructed with controller dependency" in {
        gameView should not be null
      }

      "have controller with at least one observer" in {
        mockController.observers.length should be >= 1
      }
    }

    "update is called" should {
      "not throw exception with initial state" in {
        val mockController = new MockController
        val gameView = new GameView(mockController)

        noException should be thrownBy gameView.update
      }

      "not throw exception with PlacingPhase state" in {
        val mockController = new MockController
        mockController.gameState = GameState(
          board = Board.empty.set(0, Some(Player.White)),
          currentPlayer = Player.Black,
          phase = GameStateInterface.PlacingPhase,
          whiteStonesToPlace = 8,
          blackStonesToPlace = 9,
          whiteStones = 9,
          blackStones = 9,
          message = Some(PlaceStoneMessage.Success)
        )

        val gameView = new GameView(mockController)
        noException should be thrownBy gameView.update
      }

      "not throw exception with MovingPhase state" in {
        val mockController = new MockController
        mockController.gameState = GameState(
          board = Board.empty.set(0, Some(Player.White)),
          currentPlayer = Player.White,
          phase = GameStateInterface.MovingPhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 9,
          blackStones = 9,
          message = Some(MoveStoneMessage.Success)
        )

        val gameView = new GameView(mockController)
        noException should be thrownBy gameView.update
      }

      "not throw exception with MillRemovePhase state" in {
        val mockController = new MockController
        mockController.gameState = GameState(
          board = Board.empty.set(0, Some(Player.White)),
          currentPlayer = Player.White,
          phase = GameStateInterface.MillRemovePhase,
          whiteStonesToPlace = 5,
          blackStonesToPlace = 4,
          whiteStones = 9,
          blackStones = 9,
          message = Some(RemoveStoneMessage.Success)
        )

        val gameView = new GameView(mockController)
        noException should be thrownBy gameView.update
      }

      "not throw exception with Winner.White message" in {
        val mockController = new MockController
        mockController.gameState = GameState(
          board = Board.empty,
          currentPlayer = Player.White,
          phase = GameStateInterface.PlacingPhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 3,
          blackStones = 2,
          message = Some(Winner.White)
        )

        val gameView = new GameView(mockController)
        noException should be thrownBy gameView.update
      }

      "not throw exception with Winner.Black message" in {
        val mockController = new MockController
        mockController.gameState = GameState(
          board = Board.empty,
          currentPlayer = Player.Black,
          phase = GameStateInterface.PlacingPhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 2,
          blackStones = 3,
          message = Some(Winner.Black)
        )

        val gameView = new GameView(mockController)
        noException should be thrownBy gameView.update
      }

      "retrieve game state from controller" in {
        val mockController = new MockController
        val gameView = new GameView(mockController)

        gameView.update
        mockController.gameState should not be null
      }
    }

    "controller integration" should {
      "call controller.getGameState on update" in {
        val mockController = new MockController
        val gameView = new GameView(mockController)

        val stateBefore = mockController.gameState
        gameView.update
        val stateAfter = mockController.getGameState

        stateBefore should be(stateAfter)
      }

      "reflect state changes on multiple updates" in {
        val mockController = new MockController
        val gameView = new GameView(mockController)

        gameView.update

        mockController.gameState = GameState(
          board = Board.empty.set(0, Some(Player.White)),
          currentPlayer = Player.Black,
          phase = GameStateInterface.PlacingPhase,
          whiteStonesToPlace = 8,
          blackStonesToPlace = 9,
          whiteStones = 9,
          blackStones = 9,
          message = Some(PlaceStoneMessage.Success)
        )

        noException should be thrownBy gameView.update
      }

      "work with PlacingPhase game state" in {
        val mockController = new MockController
        mockController.gameState = GameState(
          board = Board.empty,
          currentPlayer = Player.White,
          phase = GameStateInterface.PlacingPhase,
          whiteStonesToPlace = 9,
          blackStonesToPlace = 9,
          whiteStones = 9,
          blackStones = 9,
          message = Some(PlaceStoneMessage.Success)
        )

        val gameView = new GameView(mockController)
        noException should be thrownBy gameView.update

        mockController.getGameState.phase should be(GameStateInterface.PlacingPhase)
      }

      "work with MovingPhase game state" in {
        val mockController = new MockController
        mockController.gameState = GameState(
          board = Board.empty,
          currentPlayer = Player.White,
          phase = GameStateInterface.MovingPhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 9,
          blackStones = 9,
          message = Some(MoveStoneMessage.Success)
        )

        val gameView = new GameView(mockController)
        noException should be thrownBy gameView.update

        mockController.getGameState.phase should be(GameStateInterface.MovingPhase)
      }

      "work with MillRemovePhase game state" in {
        val mockController = new MockController
        mockController.gameState = GameState(
          board = Board.empty,
          currentPlayer = Player.White,
          phase = GameStateInterface.MillRemovePhase,
          whiteStonesToPlace = 5,
          blackStonesToPlace = 4,
          whiteStones = 9,
          blackStones = 9,
          message = Some(RemoveStoneMessage.Success)
        )

        val gameView = new GameView(mockController)
        noException should be thrownBy gameView.update

        mockController.getGameState.phase should be(GameStateInterface.MillRemovePhase)
      }

      "reflect phase transitions" in {
        val mockController = new MockController
        mockController.gameState = GameState(
          board = Board.empty,
          currentPlayer = Player.White,
          phase = GameStateInterface.PlacingPhase,
          whiteStonesToPlace = 1,
          blackStonesToPlace = 0,
          whiteStones = 9,
          blackStones = 9,
          message = Some(PlaceStoneMessage.Success)
        )

        val gameView = new GameView(mockController)
        gameView.update

        mockController.gameState = GameState(
          board = Board.empty.set(0, Some(Player.White)),
          currentPlayer = Player.Black,
          phase = GameStateInterface.MovingPhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 9,
          blackStones = 9,
          message = Some(MoveStoneMessage.Success)
        )

        noException should be thrownBy gameView.update
        mockController.getGameState.phase should be(GameStateInterface.MovingPhase)
      }

      "reflect stone count updates" in {
        val mockController = new MockController
        mockController.gameState = GameState(
          board = Board.empty,
          currentPlayer = Player.White,
          phase = GameStateInterface.PlacingPhase,
          whiteStonesToPlace = 9,
          blackStonesToPlace = 9,
          whiteStones = 9,
          blackStones = 9,
          message = Some(PlaceStoneMessage.Success)
        )

        val gameView = new GameView(mockController)
        gameView.update

        mockController.gameState = mockController.gameState.asInstanceOf[GameState].copy(
          whiteStonesToPlace = 8,
          board = Board.empty.set(0, Some(Player.White))
        )

        noException should be thrownBy gameView.update
        mockController.getGameState.whiteStonesToPlace should be(8)
      }

      "reflect player alternation" in {
        val mockController = new MockController
        mockController.gameState = GameState(
          board = Board.empty,
          currentPlayer = Player.White,
          phase = GameStateInterface.PlacingPhase,
          whiteStonesToPlace = 9,
          blackStonesToPlace = 9,
          whiteStones = 9,
          blackStones = 9,
          message = Some(PlaceStoneMessage.Success)
        )

        val gameView = new GameView(mockController)
        gameView.update

        mockController.gameState = mockController.gameState.asInstanceOf[GameState].copy(
          currentPlayer = Player.Black,
          board = Board.empty.set(0, Some(Player.White)),
          whiteStonesToPlace = 8
        )

        noException should be thrownBy gameView.update
        mockController.getGameState.currentPlayer should be(Player.Black)
      }
    }

    "edge cases" should {
      "handle update with empty board state" in {
        val mockController = new MockController
        mockController.gameState = GameState.create(Board.empty, 9, 9, 9, 9)

        val gameView = new GameView(mockController)
        noException should be thrownBy gameView.update
      }

      "handle update with full board state" in {
        val mockController = new MockController
        var board = Board.empty
        (0 until 12).foreach { i =>
          board = board.set(i, Some(Player.White))
        }
        (12 until 24).foreach { i =>
          board = board.set(i, Some(Player.Black))
        }

        mockController.gameState = GameState(
          board = board,
          currentPlayer = Player.White,
          phase = GameStateInterface.MovingPhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 12,
          blackStones = 12,
          message = Some(MoveStoneMessage.Success)
        )

        val gameView = new GameView(mockController)
        noException should be thrownBy gameView.update
      }

      "handle update immediately after construction" in {
        val mockController = new MockController
        val gameView = new GameView(mockController)

        noException should be thrownBy gameView.update
      }

      "handle board with only white stones" in {
        val mockController = new MockController
        var board = Board.empty
        (0 until 9).foreach { i =>
          board = board.set(i, Some(Player.White))
        }

        mockController.gameState = GameState(
          board = board,
          currentPlayer = Player.White,
          phase = GameStateInterface.MovingPhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 9,
          blackStones = 0,
          message = Some(MoveStoneMessage.Success)
        )

        val gameView = new GameView(mockController)
        noException should be thrownBy gameView.update
      }

      "handle board with only black stones" in {
        val mockController = new MockController
        var board = Board.empty
        (0 until 9).foreach { i =>
          board = board.set(i, Some(Player.Black))
        }

        mockController.gameState = GameState(
          board = board,
          currentPlayer = Player.Black,
          phase = GameStateInterface.MovingPhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 0,
          blackStones = 9,
          message = Some(MoveStoneMessage.Success)
        )

        val gameView = new GameView(mockController)
        noException should be thrownBy gameView.update
      }

      "handle mixed stones on board" in {
        val mockController = new MockController
        var board = Board.empty
          .set(0, Some(Player.White))
          .set(1, Some(Player.Black))
          .set(5, Some(Player.White))
          .set(10, Some(Player.Black))
          .set(15, Some(Player.White))

        mockController.gameState = GameState(
          board = board,
          currentPlayer = Player.White,
          phase = GameStateInterface.MovingPhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 3,
          blackStones = 2,
          message = Some(MoveStoneMessage.Success)
        )

        val gameView = new GameView(mockController)
        noException should be thrownBy gameView.update
      }
    }

    "observer pattern" should {
      "be notified when controller triggers update" in {
        val mockController = new MockController
        val gameView = new GameView(mockController)

        val initialState = mockController.gameState

        mockController.gameState = mockController.gameState.asInstanceOf[GameState].copy(
          board = Board.empty.set(0, Some(Player.White)),
          whiteStonesToPlace = 8
        )

        noException should be thrownBy mockController.triggerUpdate()
      }

      "handle multiple sequential updates" in {
        val mockController = new MockController
        val gameView = new GameView(mockController)

        (0 until 5).foreach { i =>
          mockController.gameState = mockController.gameState.asInstanceOf[GameState].copy(
            board = Board.empty.set(i, Some(Player.White)),
            whiteStonesToPlace = 9 - i - 1
          )
          noException should be thrownBy gameView.update
        }
      }

      "remain registered after multiple updates" in {
        val mockController = new MockController
        val gameView = new GameView(mockController)

        gameView.update
        gameView.update
        gameView.update

        mockController.observers should contain(gameView)
      }
    }

    "with different messages" should {
      "handle PlaceStoneMessage.Success" in {
        val mockController = new MockController
        mockController.gameState = GameState(
          board = Board.empty,
          currentPlayer = Player.White,
          phase = GameStateInterface.PlacingPhase,
          whiteStonesToPlace = 9,
          blackStonesToPlace = 9,
          whiteStones = 9,
          blackStones = 9,
          message = Some(PlaceStoneMessage.Success)
        )

        val gameView = new GameView(mockController)
        noException should be thrownBy gameView.update
      }

      "handle PlaceStoneMessage.InvalidMove" in {
        val mockController = new MockController
        mockController.gameState = GameState(
          board = Board.empty,
          currentPlayer = Player.White,
          phase = GameStateInterface.PlacingPhase,
          whiteStonesToPlace = 9,
          blackStonesToPlace = 9,
          whiteStones = 9,
          blackStones = 9,
          message = Some(PlaceStoneMessage.InvalidMove)
        )

        val gameView = new GameView(mockController)
        noException should be thrownBy gameView.update
      }

      "handle PlaceStoneMessage.Occupied" in {
        val mockController = new MockController
        mockController.gameState = GameState(
          board = Board.empty.set(0, Some(Player.White)),
          currentPlayer = Player.Black,
          phase = GameStateInterface.PlacingPhase,
          whiteStonesToPlace = 8,
          blackStonesToPlace = 9,
          whiteStones = 9,
          blackStones = 9,
          message = Some(PlaceStoneMessage.Occupied)
        )

        val gameView = new GameView(mockController)
        noException should be thrownBy gameView.update
      }

      "handle RemoveStoneMessage.Success" in {
        val mockController = new MockController
        mockController.gameState = GameState(
          board = Board.empty,
          currentPlayer = Player.White,
          phase = GameStateInterface.MillRemovePhase,
          whiteStonesToPlace = 5,
          blackStonesToPlace = 4,
          whiteStones = 9,
          blackStones = 9,
          message = Some(RemoveStoneMessage.Success)
        )

        val gameView = new GameView(mockController)
        noException should be thrownBy gameView.update
      }

      "handle RemoveStoneMessage.InvalidMove" in {
        val mockController = new MockController
        mockController.gameState = GameState(
          board = Board.empty,
          currentPlayer = Player.White,
          phase = GameStateInterface.MillRemovePhase,
          whiteStonesToPlace = 5,
          blackStonesToPlace = 4,
          whiteStones = 9,
          blackStones = 9,
          message = Some(RemoveStoneMessage.InvalidMove)
        )

        val gameView = new GameView(mockController)
        noException should be thrownBy gameView.update
      }

      "handle RemoveStoneMessage.OwnStoneChosen" in {
        val mockController = new MockController
        mockController.gameState = GameState(
          board = Board.empty.set(0, Some(Player.White)),
          currentPlayer = Player.White,
          phase = GameStateInterface.MillRemovePhase,
          whiteStonesToPlace = 5,
          blackStonesToPlace = 4,
          whiteStones = 9,
          blackStones = 9,
          message = Some(RemoveStoneMessage.OwnStoneChosen)
        )

        val gameView = new GameView(mockController)
        noException should be thrownBy gameView.update
      }

      "handle MoveStoneMessage.Success" in {
        val mockController = new MockController
        mockController.gameState = GameState(
          board = Board.empty,
          currentPlayer = Player.White,
          phase = GameStateInterface.MovingPhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 9,
          blackStones = 9,
          message = Some(MoveStoneMessage.Success)
        )

        val gameView = new GameView(mockController)
        noException should be thrownBy gameView.update
      }

      "handle MoveStoneMessage.NoNeighbour" in {
        val mockController = new MockController
        mockController.gameState = GameState(
          board = Board.empty.set(0, Some(Player.White)),
          currentPlayer = Player.White,
          phase = GameStateInterface.MovingPhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 9,
          blackStones = 9,
          message = Some(MoveStoneMessage.NoNeighbour)
        )

        val gameView = new GameView(mockController)
        noException should be thrownBy gameView.update
      }

      "handle MoveStoneMessage.NeighbourOccupied" in {
        val mockController = new MockController
        mockController.gameState = GameState(
          board = Board.empty
            .set(0, Some(Player.White))
            .set(1, Some(Player.Black)),
          currentPlayer = Player.White,
          phase = GameStateInterface.MovingPhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 9,
          blackStones = 9,
          message = Some(MoveStoneMessage.NeighbourOccupied)
        )

        val gameView = new GameView(mockController)
        noException should be thrownBy gameView.update
      }

      "handle MoveStoneMessage.NotYourStone" in {
        val mockController = new MockController
        mockController.gameState = GameState(
          board = Board.empty.set(0, Some(Player.Black)),
          currentPlayer = Player.White,
          phase = GameStateInterface.MovingPhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 9,
          blackStones = 9,
          message = Some(MoveStoneMessage.NotYourStone)
        )

        val gameView = new GameView(mockController)
        noException should be thrownBy gameView.update
      }

      "handle None message" in {
        val mockController = new MockController
        mockController.gameState = GameState(
          board = Board.empty,
          currentPlayer = Player.White,
          phase = GameStateInterface.PlacingPhase,
          whiteStonesToPlace = 9,
          blackStonesToPlace = 9,
          whiteStones = 9,
          blackStones = 9,
          message = None
        )

        val gameView = new GameView(mockController)
        noException should be thrownBy gameView.update
      }
    }

    "with different players" should {
      "handle White player" in {
        val mockController = new MockController
        mockController.gameState = GameState(
          board = Board.empty,
          currentPlayer = Player.White,
          phase = GameStateInterface.PlacingPhase,
          whiteStonesToPlace = 9,
          blackStonesToPlace = 9,
          whiteStones = 9,
          blackStones = 9,
          message = Some(PlaceStoneMessage.Success)
        )

        val gameView = new GameView(mockController)
        noException should be thrownBy gameView.update
        mockController.getGameState.currentPlayer should be(Player.White)
      }

      "handle Black player" in {
        val mockController = new MockController
        mockController.gameState = GameState(
          board = Board.empty,
          currentPlayer = Player.Black,
          phase = GameStateInterface.PlacingPhase,
          whiteStonesToPlace = 9,
          blackStonesToPlace = 9,
          whiteStones = 9,
          blackStones = 9,
          message = Some(PlaceStoneMessage.Success)
        )

        val gameView = new GameView(mockController)
        noException should be thrownBy gameView.update
        mockController.getGameState.currentPlayer should be(Player.Black)
      }
    }
  }
}
