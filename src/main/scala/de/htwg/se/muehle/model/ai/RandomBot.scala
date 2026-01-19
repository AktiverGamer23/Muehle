package de.htwg.se.muehle.ai

import de.htwg.se.muehle.gamestate.GameStateInterface
import scala.util.Random

/**
 * A simple bot that selects completely random positions.
 * Not very strategic, but useful for testing.
 */
class RandomBot extends BotStrategy:
  
  override def calculateMove(gameState: GameStateInterface): (Int, Int) =
    val from = Random.nextInt(24)
    val to = Random.nextInt(24)
    (from, to)
  
