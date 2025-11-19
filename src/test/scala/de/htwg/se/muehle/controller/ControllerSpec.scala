package de.htwg.se.muehle.controller

import de.htwg.se.muehle.model.*
import de.htwg.se.muehle.util.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class GamecontrollerSpec extends AnyWordSpec with Matchers {
    val baseState = GameState(
        board = Vector.fill(24)(Empty),
        currentPlayer = Player.White,
        whiteStonesToPlace = 9,
        blackStonesToPlace = 9,
        whiteStones = 9,
        blackStones = 9
    )


  "Gamecontroller" should {


    "update gameState and notify observers on placeStone" in {
      var notified = false

      // Einfacher Observer, der notified = true setzt
      val controller = new Gamecontroller(baseState) {
        override def notifyObservers: Unit = notified = true
      }

      controller.placeStone(0)
      controller.gameState.board(0) shouldBe Occupied(baseState.currentPlayer)
      notified shouldBe true
    }

    "update gameState and notify observers on moveStone" in {
      var notified = false

      val board = baseState.board.updated(0, Occupied(baseState.currentPlayer))
      val state = baseState.copy(board = board, whiteStonesToPlace = 0, blackStonesToPlace = 0)

      val controller = new Gamecontroller(state) {
        override def notifyObservers: Unit = notified = true
      }

      controller.moveStone(0, 1)
      controller.gameState.board(0) shouldBe Empty
      controller.gameState.board(1) shouldBe Occupied(state.currentPlayer)
      notified shouldBe true
    }

    "update gameState and notify observers on removeStone" in {
      var notified = false

      val board = baseState.board.updated(0, Occupied(Player.Black))
      val controller = new Gamecontroller(baseState.copy(board = board)) {
        override def notifyObservers: Unit = notified = true
      }

      controller.removeStone(0)
      controller.gameState.board(0) shouldBe Empty
      notified shouldBe true
    }
  }
}
