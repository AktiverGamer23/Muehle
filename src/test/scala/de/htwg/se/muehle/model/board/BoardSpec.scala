package de.htwg.se.muehle.model.board

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import de.htwg.se.muehle.board.{Board, BoardInterface}
import de.htwg.se.muehle.player.Player

class BoardSpec extends AnyWordSpec with Matchers {

  "A Board" when {
    "created empty" should {
      val board = Board.empty

      "have all positions empty" in {
        (0 until 24).foreach { pos =>
          board.stoneAt(pos) should be(None)
        }
      }

      "return None for positions >= 24" in {
        board.stoneAt(24) should be(None)
        board.stoneAt(25) should be(None)
        board.stoneAt(100) should be(None)
      }
    }

    "setting stones" should {
      val board = Board.empty

      "set a stone on vec1" in {
        val newBoard = board.set(0, Some(Player.White))
        newBoard.stoneAt(0) should be(Some(Player.White))
        newBoard.stoneAt(1) should be(None)
      }

      "set a stone on vec2" in {
        val newBoard = board.set(8, Some(Player.Black))
        newBoard.stoneAt(8) should be(Some(Player.Black))
        newBoard.stoneAt(9) should be(None)
      }

      "set a stone on vec3" in {
        val newBoard = board.set(16, Some(Player.White))
        newBoard.stoneAt(16) should be(Some(Player.White))
        newBoard.stoneAt(17) should be(None)
      }

      "set multiple stones" in {
        val newBoard = board
          .set(0, Some(Player.White))
          .set(8, Some(Player.Black))
          .set(16, Some(Player.White))

        newBoard.stoneAt(0) should be(Some(Player.White))
        newBoard.stoneAt(8) should be(Some(Player.Black))
        newBoard.stoneAt(16) should be(Some(Player.White))
      }

      "remove a stone" in {
        val newBoard = board.set(5, Some(Player.White))
        newBoard.stoneAt(5) should be(Some(Player.White))

        val clearedBoard = newBoard.set(5, None)
        clearedBoard.stoneAt(5) should be(None)
      }

      "not change board for positions >= 24" in {
        val newBoard = board.set(24, Some(Player.White))
        newBoard should be(board)

        val newBoard2 = board.set(100, Some(Player.Black))
        newBoard2 should be(board)
      }

      "update existing stone" in {
        val newBoard = board.set(3, Some(Player.White))
        newBoard.stoneAt(3) should be(Some(Player.White))

        val updatedBoard = newBoard.set(3, Some(Player.Black))
        updatedBoard.stoneAt(3) should be(Some(Player.Black))
      }
    }

    "checking neighbours" should {
      val board = Board.empty

      "recognize neighbours in same ring (next)" in {
        board.isNeighbour(0, 1) should be(true)
        board.isNeighbour(1, 2) should be(true)
        board.isNeighbour(7, 0) should be(true)
      }

      "recognize neighbours in same ring (previous)" in {
        board.isNeighbour(1, 0) should be(true)
        board.isNeighbour(0, 7) should be(true)
        board.isNeighbour(2, 1) should be(true)
      }

      "recognize neighbours across rings (odd indices)" in {
        board.isNeighbour(1, 9) should be(true)
        board.isNeighbour(9, 1) should be(true)
        board.isNeighbour(9, 17) should be(true)
        board.isNeighbour(17, 9) should be(true)
      }

      "not recognize non-neighbours" in {
        board.isNeighbour(0, 2) should be(false)
        board.isNeighbour(0, 8) should be(false)
        board.isNeighbour(0, 16) should be(false)
        board.isNeighbour(2, 10) should be(false)
      }

      "handle all ring transitions" in {
        board.isNeighbour(3, 11) should be(true)
        board.isNeighbour(11, 19) should be(true)
        board.isNeighbour(5, 13) should be(true)
        board.isNeighbour(13, 21) should be(true)
        board.isNeighbour(7, 15) should be(true)
        board.isNeighbour(15, 23) should be(true)
      }

      "handle even indices (no cross-ring connection)" in {
        board.isNeighbour(0, 8) should be(false)
        board.isNeighbour(2, 10) should be(false)
        board.isNeighbour(4, 12) should be(false)
      }
    }

    "checking for mills" should {
      val board = Board.empty

      "detect horizontal mill in ring 1 (positions 0,1,2)" in {
        val withMill = board
          .set(0, Some(Player.White))
          .set(1, Some(Player.White))
          .set(2, Some(Player.White))

        withMill.isMill(0) should be(true)
        withMill.isMill(1) should be(true)
        withMill.isMill(2) should be(true)
      }

      "detect horizontal mill in ring 1 (positions 6,7,0)" in {
        val withMill = board
          .set(6, Some(Player.Black))
          .set(7, Some(Player.Black))
          .set(0, Some(Player.Black))

        withMill.isMill(6) should be(true)
        withMill.isMill(7) should be(true)
        withMill.isMill(0) should be(true)
      }

      "detect horizontal mill in ring 2" in {
        val withMill = board
          .set(8, Some(Player.White))
          .set(9, Some(Player.White))
          .set(10, Some(Player.White))

        withMill.isMill(8) should be(true)
        withMill.isMill(9) should be(true)
        withMill.isMill(10) should be(true)
      }

      "detect horizontal mill in ring 3" in {
        val withMill = board
          .set(16, Some(Player.Black))
          .set(17, Some(Player.Black))
          .set(18, Some(Player.Black))

        withMill.isMill(16) should be(true)
        withMill.isMill(17) should be(true)
        withMill.isMill(18) should be(true)
      }

      "detect vertical mill (positions 1, 9, 17)" in {
        val withMill = board
          .set(1, Some(Player.White))
          .set(9, Some(Player.White))
          .set(17, Some(Player.White))

        withMill.isMill(1) should be(true)
        withMill.isMill(9) should be(true)
        withMill.isMill(17) should be(true)
      }

      "detect vertical mill (positions 3, 11, 19)" in {
        val withMill = board
          .set(3, Some(Player.Black))
          .set(11, Some(Player.Black))
          .set(19, Some(Player.Black))

        withMill.isMill(3) should be(true)
        withMill.isMill(11) should be(true)
        withMill.isMill(19) should be(true)
      }

      "not detect mill with empty positions" in {
        val withTwoStones = board
          .set(0, Some(Player.White))
          .set(1, Some(Player.White))

        withTwoStones.isMill(0) should be(false)
        withTwoStones.isMill(1) should be(false)
      }

      "not detect mill with different players" in {
        val withMixedStones = board
          .set(0, Some(Player.White))
          .set(1, Some(Player.Black))
          .set(2, Some(Player.White))

        withMixedStones.isMill(0) should be(false)
        withMixedStones.isMill(1) should be(false)
        withMixedStones.isMill(2) should be(false)
      }

      "not detect mill on empty position" in {
        board.isMill(0) should be(false)
        board.isMill(10) should be(false)
      }

      "detect mill at even index (backward direction)" in {
        val withMill = board
          .set(4, Some(Player.White))
          .set(5, Some(Player.White))
          .set(6, Some(Player.White))

        withMill.isMill(4) should be(true)
        withMill.isMill(5) should be(true)
        withMill.isMill(6) should be(true)
      }
    }

    "immutability" should {
      "not modify original board when setting" in {
        val original = Board.empty
        val modified = original.set(0, Some(Player.White))

        original.stoneAt(0) should be(None)
        modified.stoneAt(0) should be(Some(Player.White))
      }

      "create independent copies" in {
        val board1 = Board.empty.set(0, Some(Player.White))
        val board2 = board1.set(8, Some(Player.Black))

        board1.stoneAt(0) should be(Some(Player.White))
        board1.stoneAt(8) should be(None)

        board2.stoneAt(0) should be(Some(Player.White))
        board2.stoneAt(8) should be(Some(Player.Black))
      }
    }

    "comprehensive mill tests" should {
      "test all possible horizontal mills in ring 1" in {
        val mills = List(
          (0, 1, 2), (2, 3, 4), (4, 5, 6), (6, 7, 0)
        )

        mills.foreach { case (a, b, c) =>
          val withMill = Board.empty
            .set(a, Some(Player.White))
            .set(b, Some(Player.White))
            .set(c, Some(Player.White))

          withMill.isMill(a) should be(true)
          withMill.isMill(b) should be(true)
          withMill.isMill(c) should be(true)
        }
      }

      "test all vertical mills" in {
        val verticalMills = List(
          (1, 9, 17), (3, 11, 19), (5, 13, 21), (7, 15, 23)
        )

        verticalMills.foreach { case (a, b, c) =>
          val withMill = Board.empty
            .set(a, Some(Player.Black))
            .set(b, Some(Player.Black))
            .set(c, Some(Player.Black))

          withMill.isMill(a) should be(true)
          withMill.isMill(b) should be(true)
          withMill.isMill(c) should be(true)
        }
      }
    }

    "Board case class" should {
      "be created with default empty vectors" in {
        val board = Board()
        board.vec1.length should be(8)
        board.vec2.length should be(8)
        board.vec3.length should be(8)
        board.vec1.forall(_ == None) should be(true)
        board.vec2.forall(_ == None) should be(true)
        board.vec3.forall(_ == None) should be(true)
      }

      "be created with custom vectors" in {
        val customVec1 = Vector.fill(8)(Some(Player.White))
        val customVec2 = Vector.fill(8)(Some(Player.Black))
        val customVec3 = Vector.fill(8)(None)

        val board = Board(customVec1, customVec2, customVec3)

        board.vec1 should be(customVec1)
        board.vec2 should be(customVec2)
        board.vec3 should be(customVec3)
      }

      "support copy with modified vec1" in {
        val original = Board()
        val newVec1 = Vector.fill(8)(Some(Player.White))
        val modified = original.copy(vec1 = newVec1)

        modified.vec1 should be(newVec1)
        modified.vec2 should be(original.vec2)
        modified.vec3 should be(original.vec3)
        original.vec1 should not be newVec1
      }

      "support copy with modified vec2" in {
        val original = Board()
        val newVec2 = Vector.fill(8)(Some(Player.Black))
        val modified = original.copy(vec2 = newVec2)

        modified.vec1 should be(original.vec1)
        modified.vec2 should be(newVec2)
        modified.vec3 should be(original.vec3)
      }

      "support copy with modified vec3" in {
        val original = Board()
        val newVec3 = Vector.fill(8)(Some(Player.White))
        val modified = original.copy(vec3 = newVec3)

        modified.vec1 should be(original.vec1)
        modified.vec2 should be(original.vec2)
        modified.vec3 should be(newVec3)
      }

      "be equal when vectors are equal" in {
        val board1 = Board(
          Vector.fill(8)(Some(Player.White)),
          Vector.fill(8)(Some(Player.Black)),
          Vector.fill(8)(None)
        )
        val board2 = Board(
          Vector.fill(8)(Some(Player.White)),
          Vector.fill(8)(Some(Player.Black)),
          Vector.fill(8)(None)
        )

        board1 should be(board2)
      }

      "not be equal when vectors differ" in {
        val board1 = Board.empty
        val board2 = board1.set(0, Some(Player.White))

        board1 should not be board2
      }

      "have working toString" in {
        val board = Board.empty
        board.toString should include("Board")
      }
    }

    "Board vectors" should {
      "vec1 covers positions 0-7" in {
        val board = Board.empty
        val modifiedBoard = (0 to 7).foldLeft(board) { (b, pos) =>
          b.set(pos, Some(Player.White))
        }

        (0 to 7).foreach { pos =>
          modifiedBoard.stoneAt(pos) should be(Some(Player.White))
        }
        (8 to 23).foreach { pos =>
          modifiedBoard.stoneAt(pos) should be(None)
        }
      }

      "vec2 covers positions 8-15" in {
        val board = Board.empty
        val modifiedBoard = (8 to 15).foldLeft(board) { (b, pos) =>
          b.set(pos, Some(Player.Black))
        }

        (0 to 7).foreach { pos =>
          modifiedBoard.stoneAt(pos) should be(None)
        }
        (8 to 15).foreach { pos =>
          modifiedBoard.stoneAt(pos) should be(Some(Player.Black))
        }
        (16 to 23).foreach { pos =>
          modifiedBoard.stoneAt(pos) should be(None)
        }
      }

      "vec3 covers positions 16-23" in {
        val board = Board.empty
        val modifiedBoard = (16 to 23).foldLeft(board) { (b, pos) =>
          b.set(pos, Some(Player.White))
        }

        (0 to 15).foreach { pos =>
          modifiedBoard.stoneAt(pos) should be(None)
        }
        (16 to 23).foreach { pos =>
          modifiedBoard.stoneAt(pos) should be(Some(Player.White))
        }
      }

      "maintain independence between vectors" in {
        val board = Board.empty
          .set(0, Some(Player.White))   // vec1
          .set(8, Some(Player.Black))    // vec2
          .set(16, Some(Player.White))   // vec3

        board.stoneAt(0) should be(Some(Player.White))
        board.stoneAt(8) should be(Some(Player.Black))
        board.stoneAt(16) should be(Some(Player.White))

        val modifiedVec1 = board.set(1, Some(Player.Black))
        modifiedVec1.stoneAt(8) should be(Some(Player.Black))
        modifiedVec1.stoneAt(16) should be(Some(Player.White))
      }

      "support mixed players across all vectors" in {
        val board = Board.empty
          .set(0, Some(Player.White))
          .set(1, Some(Player.Black))
          .set(8, Some(Player.White))
          .set(9, Some(Player.Black))
          .set(16, Some(Player.White))
          .set(17, Some(Player.Black))

        board.stoneAt(0) should be(Some(Player.White))
        board.stoneAt(1) should be(Some(Player.Black))
        board.stoneAt(8) should be(Some(Player.White))
        board.stoneAt(9) should be(Some(Player.Black))
        board.stoneAt(16) should be(Some(Player.White))
        board.stoneAt(17) should be(Some(Player.Black))
      }

      "correctly map position to vector and index" in {
        val board = Board(
          vec1 = Vector(Some(Player.White), None, None, None, None, None, None, Some(Player.Black)),
          vec2 = Vector(None, Some(Player.White), None, None, None, None, None, None),
          vec3 = Vector(None, None, Some(Player.Black), None, None, None, None, None)
        )

        board.stoneAt(0) should be(Some(Player.White))
        board.stoneAt(7) should be(Some(Player.Black))
        board.stoneAt(9) should be(Some(Player.White))
        board.stoneAt(18) should be(Some(Player.Black))
      }
    }
  }
}
