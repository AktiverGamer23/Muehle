package Controller
import model.*
import View.*

//import java.lang.ModuleLayer.Controller


object GameController:
  def placingPhase(state: GameState): GameState =
    Gameview.showBoard(state)                    
    Gamelogic.validate(state) match              
      case Some(winner) =>
        println(s"Gewinner: $winner")
        state
      case None =>
        if state.whiteStonesToPlace == 0 && state.blackStonesToPlace == 0 then
          println("Platzierphase beendet!")
          state
        else
          val pos = Gameview.readPos(state.currentPlayer)
          Gamelogic.placeStone(state, pos) match
            case Some(newState) => placingPhase(newState)
            case None =>
              println("Ungültiger Zug!")
              placingPhase(state)
