package cse512

object HotzoneUtils {

  def ST_Contains(queryRectangle: String, pointString: String ): Boolean = 
  {

    val rect_coordinates = queryRectangle.split(",").map(_.trim.toDouble)
    val target_coordinates = pointString.split(",").map(_.trim.toDouble)

    val x: Double = target_coordinates(0)
    val y: Double = target_coordinates(1)
    val x1: Double = math.min(rect_coordinates(0), rect_coordinates(2))
    val y1: Double = math.min(rect_coordinates(1), rect_coordinates(3))
    val x2: Double = math.max(rect_coordinates(0), rect_coordinates(2))
    val y2: Double = math.max(rect_coordinates(1), rect_coordinates(3))

    if ((x >= x1) && (x <= x2) && (y >= y1) && (y <= y2)) {
        return true
    }
   
    return false
  }
}
