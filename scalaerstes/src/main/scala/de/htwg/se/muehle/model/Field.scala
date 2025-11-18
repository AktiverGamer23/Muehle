package de.htwg.se.muehle.model

sealed trait Field
case object Empty extends Field
case class Occupied(player: Player) extends Field

type Board = Vector[Field]

val emptyBoard: Board = Vector.fill(24)(Empty)


val neighbors: Map[Int, List[Int]] = Map(
  0 -> List(1, 7),
  1 -> List(2, 0, 9),
  2 -> List(1, 3),
  3 -> List(2, 4, 11),
  4 -> List(3, 5),
  5 -> List(6, 4,13),
  6 -> List(7, 5),
  7 -> List(0, 6, 15),
  8 -> List(9, 15),
  9 -> List(1, 8, 10, 17),
  10 -> List(9, 11),
  11 -> List(3, 10, 12, 19),
  12 -> List(11, 13),
  13 -> List(5, 12, 14, 21),
  14 -> List(15, 13),
  15 -> List(14, 7, 8, 23),
  16 -> List(17, 23),
  17 -> List(18, 16, 9),
  18 -> List(17, 19),
  19 -> List(11, 18, 20),
  20 -> List(21, 19),
  21 -> List(13, 20, 22),
  22 -> List(21, 23),
  23 -> List(15, 22, 16)
)
val mills: List[List[Int]] = List(
  List(0,1,2), List(2,3,4), List(4,5,6), List(0,7,6),
  List(8,9,10), List(10,11,12), List(12,13,14), List(14,15,8),
  List(16,17,18), List(18,19,20), List(20,21,22), List(22,23,16),
  List(1,9,17), List(3,11,19), List(5,13,21), List(7,15,23)
)