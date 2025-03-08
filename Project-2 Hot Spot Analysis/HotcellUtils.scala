package cse512

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar

object HotcellUtils {
  val coordinateStep = 0.01

  def CalculateCoordinate(inputString: String, coordinateOffset: Int): Int =
  {
    // Configuration variable:
    // Coordinate step is the size of each cell on x and y
    var result = 0
    coordinateOffset match
    {
      case 0 => result = Math.floor((inputString.split(",")(0).replace("(","").toDouble/coordinateStep)).toInt
      case 1 => result = Math.floor(inputString.split(",")(1).replace(")","").toDouble/coordinateStep).toInt
      // We only consider the data from 2009 to 2012 inclusively, 4 years in total. Week 0 Day 0 is 2009-01-01
      case 2 => {
        val timestamp = HotcellUtils.timestampParser(inputString)
        result = HotcellUtils.dayOfMonth(timestamp) // Assume every month has 31 days
      }
    }
    return result
  }

  def timestampParser (timestampString: String): Timestamp =
  {
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
    val parsedDate = dateFormat.parse(timestampString)
    val timeStamp = new Timestamp(parsedDate.getTime)
    return timeStamp
  }

  def dayOfYear (timestamp: Timestamp): Int =
  {
    val calendar = Calendar.getInstance
    calendar.setTimeInMillis(timestamp.getTime)
    return calendar.get(Calendar.DAY_OF_YEAR)
  }

  def dayOfMonth (timestamp: Timestamp): Int =
  {
    val calendar = Calendar.getInstance
    calendar.setTimeInMillis(timestamp.getTime)
    return calendar.get(Calendar.DAY_OF_MONTH)
  }

  def GetNeighbourCount(minX:Int, minY:Int, minZ:Int, maxX:Int, maxY:Int, maxZ:Int, Xin:Int, Yin:Int, Zin:Int): Int =
  {
    val mapping: Map[Int, Int] = Map(0 -> 26, 1 -> 17, 2 -> 11, 3 -> 7)

    var posMapper = 0

    if (Xin== minX || Xin == maxX){
	posMapper += 1
    }

    if (Yin == minY || Yin == maxY){
	posMapper += 1
    }

    if (Zin == minZ || Zin == maxZ){
	posMapper += 1
    }

    return mapping.get(posMapper).get.toInt
  }

  def GetisOrd(x: Int, y: Int, z: Int, numcells: Int, mean:Double, sd: Double, totalNeighbours: Int, sumAllNeighboursPoints: Int): Double =
  {
    val numerator = (sumAllNeighboursPoints.toDouble - (mean*totalNeighbours.toDouble))
    val denominator = sd * math.sqrt((((numcells.toDouble * totalNeighbours.toDouble) - (totalNeighbours.toDouble * totalNeighbours.toDouble)) / (numcells.toDouble-1.0).toDouble).toDouble).toDouble
    return (numerator/denominator).toDouble
  } 
}
