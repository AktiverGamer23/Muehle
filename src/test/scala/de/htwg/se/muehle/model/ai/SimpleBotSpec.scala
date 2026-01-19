package de.htwg.se.muehle.model.ai

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.se.muehle.ai.SimpleBot
import de.htwg.se.muehle.gamestate.GameState
import de.htwg.se.muehle.board.Board

class SimpleBotSpec extends AnyWordSpec with Matchers {

  "A SimpleBot" when {
    val bot = new SimpleBot()
    val gameState = GameState.create(Board.empty, 9, 9, 9, 9)

    "calculating a move" should {
      val (from, to) = bot.calculateMove(gameState)

      "return valid from position in range 0-11" in {
        from should be >= 0
        from should be < 12
      }

      "return valid to position in range 0-11" in {
        to should be >= 0
        to should be < 12
      }

      "return a tuple of two Ints" in {
        bot.calculateMove(gameState) shouldBe an [Any]
      }
    }

    "called multiple times" should {
      "generate moves within valid range" in {
        val moves = (1 to 100).map(_ => bot.calculateMove(gameState))

        moves.foreach { case (from, to) =>
          from should be >= 0
          from should be < 12
          to should be >= 0
          to should be < 12
        }
      }

      "potentially generate different moves" in {
        val move1 = bot.calculateMove(gameState)
        val move2 = bot.calculateMove(gameState)
        val move3 = bot.calculateMove(gameState)

        val moves = Set(move1, move2, move3)
        moves.size should be > 0
      }
    }

    "compared to RandomBot" should {
      "use smaller range" in {
        val simpleBot = new SimpleBot()
        val moves = (1 to 50).map(_ => simpleBot.calculateMove(gameState))

        moves.foreach { case (from, to) =>
          from should be < 12
          to should be < 12
        }
      }
    }

    "with different game states" should {
      "work with any game state" in {
        val boardWithStones = Board.empty
          .set(0, Some(de.htwg.se.muehle.player.Player.White))
          .set(8, Some(de.htwg.se.muehle.player.Player.Black))

        val gameState2 = GameState(
          board = boardWithStones,
          currentPlayer = de.htwg.se.muehle.player.Player.White,
          phase = de.htwg.se.muehle.gamestate.GameStateInterface.MovingPhase,
          whiteStonesToPlace = 0,
          blackStonesToPlace = 0,
          whiteStones = 9,
          blackStones = 9,
          message = None
        )

        val (from, to) = bot.calculateMove(gameState2)
        from should be >= 0
        from should be < 12
        to should be >= 0
        to should be < 12
      }
    }

    "creating multiple instances" should {
      "work independently" in {
        val bot1 = new SimpleBot()
        val bot2 = new SimpleBot()

        val move1 = bot1.calculateMove(gameState)
        val move2 = bot2.calculateMove(gameState)

        move1 shouldBe an [Any]
        move2 shouldBe an [Any]
      }
    }
  }
}
