package de.htwg.se.muehle.model

case class Board(
  vec1: Vector[Option[Player]],
  vec2: Vector[Option[Player]],
  vec3: Vector[Option[Player]]
)

object Board:
  val empty: Board =
    Board(
      Vector.fill(8)(None),
      Vector.fill(8)(None),
      Vector.fill(8)(None)
    )


  def set(board: Board, pos: Int, player: Option[Player]): Board =
    

    val vec = pos / 8
    val index = pos % 8

    vec match
      case 0 => board.copy(vec1 = board.vec1.updated(index, player))
      case 1 => board.copy(vec2 = board.vec2.updated(index, player))
      case 2 => board.copy(vec3 = board.vec3.updated(index, player))