// BotSelector.scala
package de.htwg.se.muehle.ai

import com.google.inject.{Inject, Singleton}
import de.htwg.se.muehle.gamestate.GameStateInterface

@Singleton
class BotSelector @Inject() () extends BotSelectorInterface:
  
  private var currentStrategy: Option[BotStrategy] = None
  
  override def setStrategy(strategy: BotStrategy): Unit =
    currentStrategy = Some(strategy)
  
  override def getStrategy: Option[BotStrategy] = currentStrategy
  
  override def clearStrategy(): Unit =
    currentStrategy = None
  
  override def isActive: Boolean = currentStrategy.isDefined
  
  override def calculateMove(gameState: GameStateInterface): Option[(Int, Int)] =
    currentStrategy.map(_.calculateMove(gameState))