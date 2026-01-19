// StateP.scala
package de.htwg.se.muehle.state

import com.google.inject.Singleton
import de.htwg.se.muehle.model.{PlaceStoneMessage, RemoveStoneMessage, MoveStoneMessage}
import de.htwg.se.muehle.player.Player
import de.htwg.se.muehle.gamestate.GameStateInterface

@Singleton
class StateP extends StateInterface:

  override def handle(gs: GameStateInterface, pos1: Int, pos2: Int = -1): GameStateInterface =
    gs.phase match
      case GameStateInterface.PlacingPhase     => handlePlacing(gs, pos1)
      case GameStateInterface.MovingPhase      => handleMoving(gs, pos1, pos2)
      case GameStateInterface.MillRemovePhase  => handleMillRemove(gs, pos1)

  private def handlePlacing(gs: GameStateInterface, pos: Int): GameStateInterface =
    if pos < 0 || pos >= 24 then
      gs.asInstanceOf[de.htwg.se.muehle.gamestate.GameState].copy(
        message = Some(PlaceStoneMessage.InvalidMove)
      )
    else gs.getPlayer(pos) match
      case Some(_) =>
        gs.asInstanceOf[de.htwg.se.muehle.gamestate.GameState].copy(
          message = Some(PlaceStoneMessage.Occupied)
        )
      case None =>
        val (newWhite, newBlack) = gs.currentPlayer match
          case Player.White => (gs.whiteStonesToPlace - 1, gs.blackStonesToPlace)
          case Player.Black => (gs.whiteStonesToPlace, gs.blackStonesToPlace - 1)

        val newBoard = gs.board.set(pos, Some(gs.currentPlayer))

        val nextPhase =
          if newBoard.isMill(pos) then GameStateInterface.MillRemovePhase
          else if newWhite == 0 && newBlack == 0 then GameStateInterface.MovingPhase
          else GameStateInterface.PlacingPhase

        val nextPlayer =
          if nextPhase == GameStateInterface.MillRemovePhase then gs.currentPlayer
          else gs.currentPlayer.next

        val nextMessage =
          if nextPhase == GameStateInterface.MillRemovePhase then Some(RemoveStoneMessage.Success)
          else if nextPhase == GameStateInterface.MovingPhase then Some(MoveStoneMessage.Success)
          else Some(PlaceStoneMessage.Success)

        gs.asInstanceOf[de.htwg.se.muehle.gamestate.GameState].copy(
          board = newBoard,
          whiteStonesToPlace = newWhite,
          blackStonesToPlace = newBlack,
          currentPlayer = nextPlayer,
          phase = nextPhase,
          message = nextMessage
        )

  private def handleMoving(gs: GameStateInterface, from: Int, to: Int): GameStateInterface =
    if from < 0 || from >= 24 || to < 0 || to >= 24 then
      gs.asInstanceOf[de.htwg.se.muehle.gamestate.GameState].copy(
        message = Some(MoveStoneMessage.NoNeighbour)
      )
    else (gs.getPlayer(from), gs.getPlayer(to)) match
      case (Some(owner), None) if owner == gs.currentPlayer =>
        if !gs.board.isNeighbour(from, to) then
          gs.asInstanceOf[de.htwg.se.muehle.gamestate.GameState].copy(
            message = Some(MoveStoneMessage.NoNeighbour)
          )
        else
          val newBoard = gs.board
            .set(from, None)
            .set(to, Some(gs.currentPlayer))

          val nextPhase =
            if newBoard.isMill(to) then GameStateInterface.MillRemovePhase
            else GameStateInterface.MovingPhase

          val nextPlayer =
            if nextPhase == GameStateInterface.MillRemovePhase then gs.currentPlayer
            else gs.currentPlayer.next

          val nextMessage =
            if nextPhase == GameStateInterface.MillRemovePhase then Some(RemoveStoneMessage.Success)
            else Some(MoveStoneMessage.Success)

          gs.asInstanceOf[de.htwg.se.muehle.gamestate.GameState].copy(
            board = newBoard,
            currentPlayer = nextPlayer,
            phase = nextPhase,
            message = nextMessage
          )

      case (Some(_), Some(_)) =>
        gs.asInstanceOf[de.htwg.se.muehle.gamestate.GameState].copy(
          message = Some(MoveStoneMessage.NeighbourOccupied)
        )
      case _ =>
        gs.asInstanceOf[de.htwg.se.muehle.gamestate.GameState].copy(
          message = Some(MoveStoneMessage.NoNeighbour)
        )

  private def handleMillRemove(gs: GameStateInterface, pos: Int): GameStateInterface =
    if pos < 0 || pos >= 24 then
      gs.asInstanceOf[de.htwg.se.muehle.gamestate.GameState].copy(
        message = Some(RemoveStoneMessage.InvalidMove)
      )
    else gs.getPlayer(pos) match
      case Some(owner) =>
        if owner == gs.currentPlayer then
          gs.asInstanceOf[de.htwg.se.muehle.gamestate.GameState].copy(
            message = Some(RemoveStoneMessage.OwnStoneChosen)
          )
        else
          val newWhiteStones =
            if owner == Player.White then gs.whiteStones - 1 else gs.whiteStones
          val newBlackStones =
            if owner == Player.Black then gs.blackStones - 1 else gs.blackStones

          // Check for winner (player has only 2 stones left)
          if newWhiteStones <= 2 then
            gs.asInstanceOf[de.htwg.se.muehle.gamestate.GameState].copy(
              board = gs.board.set(pos, None),
              whiteStones = newWhiteStones,
              blackStones = newBlackStones,
              message = Some(de.htwg.se.muehle.model.Winner.Black)
            )
          else if newBlackStones <= 2 then
            gs.asInstanceOf[de.htwg.se.muehle.gamestate.GameState].copy(
              board = gs.board.set(pos, None),
              whiteStones = newWhiteStones,
              blackStones = newBlackStones,
              message = Some(de.htwg.se.muehle.model.Winner.White)
            )
          else
            val nextPhase =
              if !gs.isPlacingPhaseOver then GameStateInterface.PlacingPhase
              else GameStateInterface.MovingPhase

            val nextMessage =
              if !gs.isPlacingPhaseOver then Some(PlaceStoneMessage.Success)
              else Some(MoveStoneMessage.Success)

            gs.asInstanceOf[de.htwg.se.muehle.gamestate.GameState].copy(
              board = gs.board.set(pos, None),
              whiteStones = newWhiteStones,
              blackStones = newBlackStones,
              currentPlayer = gs.currentPlayer.next,
              phase = nextPhase,
              message = nextMessage
            )

      case None =>
        gs.asInstanceOf[de.htwg.se.muehle.gamestate.GameState].copy(
          message = Some(RemoveStoneMessage.InvalidMove)
        )