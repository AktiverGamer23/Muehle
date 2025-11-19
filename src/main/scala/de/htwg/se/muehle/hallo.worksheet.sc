println("Mühle")    
   
val x = 1

val a = 2
val Feld: Array[String] = Array("·", "X", "O","·")

val spielfeld: String =
  """
 ·-----------·-----------·
 |           |           |
 |   ·-------·-------·   |
 |   |       |       |   |
 |   |   ·---·---·   |   |
 |   |       |       |   |
 ·---·---·       ·---·---·
 |   |       |       |   |
 |   |   ·---·---·   |   |
 |   |       |       |   |
 |   ·-------·-------·   |
 |           |           |
 ·-----------·-----------·
  """

println(x)
val g = 3
g + a
class Ring(val id: Int, val player: String, var n:List[Int])

val b = new Ring(1,"X", List(2,3,4))
sealed trait Field
case object Empty extends Field
case class Occupied(player: Player) extends Field
type Board = Vector[Field]


val emptyBoard: Board = Vector.fill(24)(Empty)


def mill(board: Board,pos: Int): List =
   val mills = List(
   List(0,1,2), List(2,3,4), List(4,5,6),List(6,7,0),List(8,9,10),List(10,11,12), List(12,13,14),List(14,15,8),
   List(16,17,18),List(18,19,20),List(20,21,22),List(22,23,16),List(1,9,17),List(3,11,19),List(5,13,21),List(7,15,23)
  )
  mills.exists(mill => mill.contains(pos) && 1 == 1)

mill(emptyBoard,2)
