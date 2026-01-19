package de.htwg.se.muehle.board

import de.htwg.se.muehle.player.Player

trait BoardInterface:
  def stoneAt(pos: Int): Option[Player]
  def set(pos: Int, player: Option[Player]): BoardInterface
  def isNeighbour(from: Int, to: Int): Boolean
  def isMill(pos: Int): Boolean
