package scala
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
      validate(state) shouldBe Some(Player.Black)
    }
  
    "return White" in {
      val state = GameState(Vector.fill(24)(Empty), Player.White, 9, 9, 9, 2)
      validate(state) shouldBe Some(Player.White)
    }
    "return None" in {
      val state = GameState(Vector.fill(24)(Empty), Player.White, 9, 9, 3, 3)
      validate(state) shouldBe None
    }
  }
  "Placing Stone" should {
    "return None when field is occupied" in {
        val state = GameState(Vector.fill(24)(Occupied(Player.Black)), Player.White, 9, 9, 9, 9)
        placeStone(state, 2) shouldBe None

    }
    "Shoud Place a White Stone and switch to Black" in {
        val state = GameState(Vector.fill(24)(Empty), Player.White, 9, 9, 9, 9)
        val result = placeStone(state, 0)
        result should not be None
        val newstate = result.get 
        newstate.board(0) shouldBe Occupied(Player.White)
        newstate.whiteStonesToPlace shouldBe 8
        newstate.currentPlayer shouldBe Player.Black
    }
    "Should Place a Black Stone and switch to White" in{
        val state = GameState(Vector.fill(24)(Empty), Player.Black, 9, 9, 9, 9)
        val result = placeStone(state, 0)
        result should not be None
        val newstate = result.get 
        newstate.board(0) shouldBe Occupied(Player.Black)
        newstate.blackStonesToPlace shouldBe 8
        newstate.currentPlayer shouldBe Player.White
    }
  }
  "Placing" should {
    "place   stones correctly, finish placing" in {
      val initialState = GameState(Vector.fill(24)(Empty), Player.White, 0, 0, 0, 0)
      
      initialState.whiteStonesToPlace shouldBe 0
      initialState.blackStonesToPlace shouldBe 0
   
    }

    "retry on invalid move (field already occupied)" in {
     
      val input = "0\n0\n1\n"
      val inStream = new ByteArrayInputStream(input.getBytes)
      Console.withIn(inStream) {
      val initialState = GameState(Vector.fill(24)(Empty),Player.White,1,1,0,0)

      val finalState = Placing(initialState)
      finalState.board(0) shouldBe Occupied(Player.White)
      finalState.board(1) shouldBe Occupied(Player.Black)
      finalState.whiteStonesToPlace shouldBe 0
      finalState.blackStonesToPlace shouldBe 0
      }
    }
  }
  "runMuehle" should {
  "run Muehle" in {
      
      val input = "3\n0\n1\n2\n3\n4\n5\n6"
      val inStream = new ByteArrayInputStream(input.getBytes)

     
      Console.withIn(inStream) {
      MuehleApp.runMuehle()  
       
    }
    }
  }
}


