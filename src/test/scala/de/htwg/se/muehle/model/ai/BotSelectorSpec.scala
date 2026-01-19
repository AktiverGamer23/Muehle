package de.htwg.se.muehle.model.ai

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.se.muehle.ai.{BotSelector, BotStrategy, RandomBot, SimpleBot}
import de.htwg.se.muehle.gamestate.{GameState, GameStateInterface}
import de.htwg.se.muehle.board.Board
import de.htwg.se.muehle.player.Player

class BotSelectorSpec extends AnyWordSpec with Matchers {

  "A BotSelector" when {
    val gameState = GameState.create(Board.empty, 9, 9, 9, 9)

    "initially created" should {
      val selector = new BotSelector()

      "have no strategy set" in {
        selector.getStrategy should be(None)
      }

      "not be active" in {
        selector.isActive should be(false)
      }

      "return None when calculating move without strategy" in {
        selector.calculateMove(gameState) should be(None)
      }
    }

    "setting a strategy" should {
      val selector = new BotSelector()
      val bot = new RandomBot()
      selector.setStrategy(bot)

      "store the strategy" in {
        selector.getStrategy should be(Some(bot))
      }

      "be active" in {
        selector.isActive should be(true)
      }

      "calculate moves using the strategy" in {
        val result = selector.calculateMove(gameState)
        result should not be None
        result.get shouldBe an [Any]
      }
    }

    "changing strategy" should {
      val bot1 = new RandomBot()
      val bot2 = new SimpleBot()

      val selector2 = new BotSelector()
      selector2.setStrategy(bot1)
      selector2.getStrategy should be(Some(bot1))

      selector2.setStrategy(bot2)

      "update to new strategy" in {
        selector2.getStrategy should be(Some(bot2))
      }

      "still be active" in {
        selector2.isActive should be(true)
      }
    }

    "clearing strategy" should {
      val selector3 = new BotSelector()
      selector3.setStrategy(new RandomBot())
      selector3.clearStrategy()

      "remove the strategy" in {
        selector3.getStrategy should be(None)
      }

      "not be active" in {
        selector3.isActive should be(false)
      }

      "return None when calculating move" in {
        selector3.calculateMove(gameState) should be(None)
      }
    }

    "with different bot strategies" should {
      "work with RandomBot" in {
        val selector4 = new BotSelector()
        val randomBot = new RandomBot()
        selector4.setStrategy(randomBot)

        val result = selector4.calculateMove(gameState)
        result should be(defined)
        val (from, to) = result.get
        from should be >= 0
        from should be < 24
        to should be >= 0
        to should be < 24
      }

      "work with SimpleBot" in {
        val selector5 = new BotSelector()
        val simpleBot = new SimpleBot()
        selector5.setStrategy(simpleBot)

        val result = selector5.calculateMove(gameState)
        result should be(defined)
        val (from, to) = result.get
        from should be >= 0
        from should be < 12
        to should be >= 0
        to should be < 12
      }
    }

    "multiple instances" should {
      "be independent" in {
        val selector1 = new BotSelector()
        val selector2 = new BotSelector()

        selector1.setStrategy(new RandomBot())

        selector1.isActive should be(true)
        selector2.isActive should be(false)
      }
    }
  }
}
