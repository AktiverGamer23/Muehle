package de.htwg.se.muehle.ai

import de.htwg.se.muehle.gamestate.GameStateInterface

trait BotStrategy:
  /**
   * Calculates the next move for the bot.
   * 
   * @param gameState The current game state
   * @return A tuple (pos1, pos2) representing the bot's move
   */
  def calculateMove(gameState: GameStateInterface): (Int, Int)