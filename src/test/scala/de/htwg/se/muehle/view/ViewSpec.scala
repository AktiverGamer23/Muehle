package de.htwg.se.muehle.view

import de.htwg.se.muehle.controller.Gamecontroller
import de.htwg.se.muehle.model.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import java.io.ByteArrayInputStream

class GameViewSpec extends AnyWordSpec with Matchers {

  // Einfacher BaseState für Tests
  val baseState: GameState = GameState(
    board = Vector.fill(24)(Empty),
    currentPlayer = Player.White,
    whiteStonesToPlace = 9,
    blackStonesToPlace = 9,
    whiteStones = 9,
    blackStones = 9
  )

  "GameView" should {

    "call update when notified by controller" in {
      val controller = new Gamecontroller(baseState)
      var updated = false

      // Subclass für Test, überschreibt update
      val view = new GameView(controller) {
        override def update: Unit = updated = true
      }

      controller.notifyObservers  // Observer benachrichtigen
      updated shouldBe true
    }

    "display the board" in {
      val controller = new Gamecontroller(baseState)
      val view = new GameView(controller)

      // Einfach prüfen, dass display keine Exception wirft
   //   noException should be thrownBy view.display(baseState)
    }

    "read a valid integer input" in {
      val controller = new Gamecontroller(baseState)
      val view = new GameView(controller)

      val input = new ByteArrayInputStream("5\n".getBytes)
      val result = scala.Console.withIn(input) {
        view.readValidInt("Prompt:")
      }

      result shouldBe 5
    }

    "retry on invalid input and finally return valid integer" in {
      val controller = new Gamecontroller(baseState)
      val view = new GameView(controller)

      val input = new ByteArrayInputStream("abc\n30\n7\n".getBytes)
      val result = scala.Console.withIn(input) {
        view.readValidInt("Prompt:")
      }

      result shouldBe 7
    }
  }
}
