@main def Spiel(): Unit =
  println("Mühle")
  val Feld: Array[String] = Array("·", "X", "O","·")
  class Ring(val id: Int, val player: String, var n:List[Int])

  val b = new Ring(1,"X", List(2,3,4))
  
  val spielfeld: String =
    s"""
     ${b.player}-----------${Feld(1)}-----------${Feld(2)}
     |           |           |
     |   ·-------·-------·   |
     |   |       |       |   |
     |   |   ·---·---·   |   |
     |   |   |       |   |   |
     ·---·---·       ·---·---·
     |   |   |       |   |   |
     |   |   ·---·---·   |   |
     |   |       |       |   |
     |   ·-------·-------·   |
     |           |           |
     ·-----------·-----------·
    """

  println(spielfeld)


