package scala
import model.*
import View.*
import Controller.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import java.io.ByteArrayInputStream


class MySuite extends AnyWordSpec with Matchers {
  "player.next" should{
    "be black when" in{
        Player.White.next shouldBe Player.Black
    }
    "be white when" in{
        Player.Black.next shouldBe Player.White
    }
  }
  "validate" should {
    "return Black" in {
      val state = GameState(Vector.fill(24)(Empty), Player.White, 9, 9, 2, 9)
      Gamelogic.validate(state) shouldBe Some(Player.Black)
    }
  
    "return White" in {
      val state = GameState(Vector.fill(24)(Empty), Player.White, 9, 9, 9, 2)
      Gamelogic.validate(state) shouldBe Some(Player.White)
    }
    "return None" in {
      val state = GameState(Vector.fill(24)(Empty), Player.White, 9, 9, 3, 3)
      Gamelogic.validate(state) shouldBe None
    }
  }
  "Placing Stone" should {
    "return None when field is occupied" in {
        val state = GameState(Vector.fill(24)(Occupied(Player.Black)), Player.White, 9, 9, 9, 9)
        Gamelogic.placeStone(state, 2) shouldBe None

    }
    "Shoud Place a White Stone and switch to Black" in {
        val state = GameState(Vector.fill(24)(Empty), Player.White, 9, 9, 9, 9)
        val result = Gamelogic.placeStone(state, 0)
        result should not be None
        val newstate = result.get 
        newstate.board(0) shouldBe Occupied(Player.White)
        newstate.whiteStonesToPlace shouldBe 8
        newstate.currentPlayer shouldBe Player.Black
    }
    "Should Place a Black Stone and switch to White" in{
        val state = GameState(Vector.fill(24)(Empty), Player.Black, 9, 9, 9, 9)
        val result = Gamelogic.placeStone(state, 0)
        result should not be None
        val newstate = result.get 
        newstate.board(0) shouldBe Occupied(Player.Black)
        newstate.blackStonesToPlace shouldBe 8
        newstate.currentPlayer shouldBe Player.White
    }
  }
  "Placing" should {
    "finish placing" in {
      val initialState = GameState(Vector.fill(24)(Empty), Player.White, 0, 0, 0, 0)
      
      initialState.whiteStonesToPlace shouldBe 0
      initialState.blackStonesToPlace shouldBe 0
   
    }
    "should not pick a winner in the beginning" in {
      val input = "3\n2\nA\n24\n0\n1\n5\n6"
      val inStream = new ByteArrayInputStream(input.getBytes)
      Console.withIn(inStream) {
      val initialState = GameState(Vector.fill(24)(Empty), Player.White, 3, 3, 3, 3)

      val afterWhite = GameController.placingPhase(initialState)

      val afterBlack = GameController.placingPhase(afterWhite)

      afterBlack.whiteStonesToPlace shouldBe 0
      afterBlack.blackStonesToPlace shouldBe 0
  }
}

    
    

    "retry invalid move" in {
     
      val initialState = GameState(Vector.fill(24)(Empty), Player.White, 1, 1, 0, 0)

// White setzt auf 0
      val afterWhite = Gamelogic.placeStone(initialState, 0).get
      afterWhite.board(0).shouldBe(Occupied(Player.White))
      afterWhite.whiteStonesToPlace shouldBe 0

// Black setzt auf 1
      val afterBlack = Gamelogic.placeStone(afterWhite, 1).get
      afterBlack.board(1).shouldBe(Occupied(Player.Black))
      afterBlack.blackStonesToPlace shouldBe 0

      
    }
  }
  "runMuehle" should {
  "run Muehle" in {
      
      val input = "23\nA\n0\n"
      val inStream = new ByteArrayInputStream(input.getBytes)
      Console.withIn(inStream) {
      MuehleApp.runMuehle()  
       
    }
    }
  }
}


