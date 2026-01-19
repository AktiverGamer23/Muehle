package de.htwg.se.muehle.state

import de.htwg.se.muehle.gamestate.GameStateInterface

trait StateInterface:
  def handle(gs: GameStateInterface, pos1: Int, pos2: Int = -1): GameStateInterface
