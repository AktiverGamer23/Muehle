package de.htwg.se.muehle.ai

import de.htwg.se.muehle.gamestate.GameStateInterface
import scala.util.Random

/**
 * A simple bot with slightly more constrained random selection.
 * Uses positions 0-11 instead of full board range.
 */
class SimpleBot extends BotStrategy:
  
  override def calculateMove(gameState: GameStateInterface): (Int, Int) =
    val from = Random.nextInt(12)
    val to = Random.nextInt(12)
    (from, to)
  
