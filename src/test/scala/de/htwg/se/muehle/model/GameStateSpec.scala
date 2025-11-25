package de.htwg.se.muehle.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class GameStateSpec extends AnyWordSpec with Matchers {


  def baseState: GameState =
    GameState(
      currentPlayer = Player.White,
      whiteStonesToPlace = 9,
      blackStonesToPlace = 9,
      whiteStones = 9,
      blackStones = 9
    )
    

}