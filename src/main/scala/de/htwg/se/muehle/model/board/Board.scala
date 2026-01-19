package de.htwg.se.muehle.board

import de.htwg.se.muehle.player.Player


case class Board(
  vec1: Vector[Option[Player]] = Vector.fill(8)(None),
  vec2: Vector[Option[Player]] = Vector.fill(8)(None),
  vec3: Vector[Option[Player]] = Vector.fill(8)(None)
) extends BoardInterface:

  override def stoneAt(pos: Int): Option[Player] =
    val vec = pos / 8
    val index = pos % 8
    vec match
      case 0 => vec1(index)
      case 1 => vec2(index)
      case 2 => vec3(index)
      case _ => None

  override def set(pos: Int, player: Option[Player]): BoardInterface =
    val vec = pos / 8
    val index = pos % 8
    vec match
      case 0 => this.copy(vec1 = vec1.updated(index, player))
      case 1 => this.copy(vec2 = vec2.updated(index, player))
      case 2 => this.copy(vec3 = vec3.updated(index, player))
      case _ => this

  override def isNeighbour(from: Int, to: Int): Boolean =
    val fromVec = from / 8
    val fromIdx = from % 8
    val toVec = to / 8
    val toIdx = to % 8
    val invec = (fromVec == toVec) && ((fromIdx + 1) % 8 == toIdx || (fromIdx + 7) % 8 == toIdx)
    val ovec = (fromIdx % 2 != 0) && (math.abs(fromVec - toVec) == 1) && (fromIdx == toIdx)
    invec || ovec

  override def isMill(pos: Int): Boolean =
    val vec = pos / 8
    val idx = pos % 8
    stoneAt(pos).exists { player =>
      val ring = vec match
        case 0 => vec1
        case 1 => vec2
        case 2 => vec3
      val i1 = (idx + 1) % 8
      val i2 = (idx + 2) % 8
      val i7 = (idx + 7) % 8
      val i6 = (idx + 6) % 8
      if idx % 2 == 0 then
        (ring(i1).contains(player) && ring(i2).contains(player)) ||
        (ring(i7).contains(player) && ring(i6).contains(player))
      else
        (ring(i1).contains(player) && ring(i7).contains(player)) ||
        (vec1(idx).contains(player) && vec2(idx).contains(player) && vec3(idx).contains(player))
    }


object Board:
  def empty: BoardInterface =
    Board(
      Vector.fill(8)(None),
      Vector.fill(8)(None),
      Vector.fill(8)(None)
    )