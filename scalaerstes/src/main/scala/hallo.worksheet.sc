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
