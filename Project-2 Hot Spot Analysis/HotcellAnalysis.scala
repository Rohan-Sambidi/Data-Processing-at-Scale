package cse512

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.functions._

object HotcellAnalysis {
    Logger.getLogger("org.spark_project").setLevel(Level.WARN)
    Logger.getLogger("org.apache").setLevel(Level.WARN)
    Logger.getLogger("akka").setLevel(Level.WARN)
    Logger.getLogger("com").setLevel(Level.WARN)

    def runHotcellAnalysis(spark: SparkSession, pointPath: String): DataFrame =
    {
      // Load the original data from a data source
      var pickupInfo = spark.read.format("com.databricks.spark.csv").option("delimiter",";").option("header","false").load(pointPath);
      pickupInfo.createOrReplaceTempView("nyctaxitrips")
      pickupInfo.show()

      // Assign cell coordinates based on pickup points
      spark.udf.register("CalculateX",(pickupPoint: String)=>((
       HotcellUtils.CalculateCoordinate(pickupPoint, 0)
      )))
     spark.udf.register("CalculateY",(pickupPoint: String)=>((
      HotcellUtils.CalculateCoordinate(pickupPoint, 1)
     )))
     spark.udf.register("CalculateZ",(pickupTime: String)=>((
      HotcellUtils.CalculateCoordinate(pickupTime, 2)
      )))
     pickupInfo = spark.sql("select CalculateX(nyctaxitrips._c5),CalculateY(nyctaxitrips._c5), CalculateZ(nyctaxitrips._c1) from nyctaxitrips")
  
    var newCoordinateName = Seq("x", "y", "z")
    pickupInfo = pickupInfo.toDF(newCoordinateName:_*)
    pickupInfo.show()

    // Define the min and max of x, y, z
    val minX = -74.50/HotcellUtils.coordinateStep
    val maxX = -73.70/HotcellUtils.coordinateStep
    val minY = 40.50/HotcellUtils.coordinateStep
    val maxY = 40.90/HotcellUtils.coordinateStep
    val minZ = 1
    val maxZ = 31
    val numCells = (maxX - minX + 1)*(maxY - minY + 1)*(maxZ - minZ + 1)

    pickupInfo.createOrReplaceTempView("pickupInfoView")

    // Get all x, y, z points which are inside the given boundary
    val filteredPointsDf = spark.sql("select x,y,z from pickupInfoView where x >= " + minX + " AND y >= " + minY + " AND z >= " + minZ + " AND x <= " + maxX + " AND y <= " + maxY + " AND z <= " + maxZ + " order by z,y,x").persist()
    filteredPointsDf.createOrReplaceTempView("filteredPointsView")

    // Count all the picksups from same cell
    // x, y, z represents a cell and numPoints represents the number of pickups in that cell
    val filteredPointCountDf = spark.sql("select x,y,z,count(*) as numPoints from filteredPointsView group by z,y,x order by z,y,x").persist()
    filteredPointCountDf.createOrReplaceTempView("filteredPointCountDfView")

    // Define the variables that are needed for calculating Getis-Ord statistic.
    spark.udf.register("square", (inputX: Int) => (inputX*inputX).toDouble) // Function to compute square of a number.
    val sumofPoints = spark.sql("select count(*) as numCellsWithAtleastOnePoint, sum(numPoints) as totalPointsInsideTheGivenArea, sum(square(numPoints)) as squaredSumOfAllPointsInGivenArea from filteredPointCountDfView")
    sumofPoints.createOrReplaceTempView("sumofPoints")

    val numCellsWithAtleastOnePoint = sumofPoints.first().getLong(0)
    val totalPoints = sumofPoints.first().getLong(1) //sigma xj
    val squaredsumcells = sumofPoints.first().getDouble(2) //sigma (xj**2)

    val Xbar = totalPoints / numCells
    val SD = math.sqrt((squaredsumcells / numCells) - (Xbar * Xbar) )

    // Function to get the neighbours of a cell
    spark.udf.register("GetNeighbourCount", (minX: Int, minY: Int, minZ: Int, maxX: Int, maxY: Int, maxZ: Int, Xin: Int, Yin: Int, Zin: Int) => ((HotcellUtils.GetNeighbourCount(minX, minY, minZ, maxX, maxY, maxZ, Xin, Yin, Zin))))
    val Neighbours = spark.sql("select view1.x as x, view1.y as y, view1.z as z, " +
                               "GetNeighbourCount("+minX + "," + minY + "," + minZ + "," + maxX + "," + maxY + "," + maxZ + "," + "view1.x,view1.y,view1.z) as totalNeighbours, " +   // function to get the number of neighbours of x,y,z
                               "count(*) as neighboursWithValidPoints, " +  // count the neighbours with atLeast one pickup point
                               "sum(view2.numPoints) as sumAllNeighboursPoints " + // add those points
                               "from filteredPointCountDfView as view1, filteredPointCountDfView as view2 " +    // two tables to join
                               "where (view2.x = view1.x+1 or view2.x = view1.x or view2.x = view1.x-1) and (view2.y = view1.y+1 or view2.y = view1.y or view2.y = view1.y-1) and (view2.z = view1.z+1 or view2.z = view1.z or view2.z = view1.z-1) " +   //join condition
                               "group by view1.z, view1.y, view1.x order by view1.z, view1.y, view1.x").persist()
    Neighbours.createOrReplaceTempView("NeighboursView")

    // Function to calculate the statistic
    spark.udf.register("GetisOrd", (x: Int, y: Int, z: Int, numcells: Int, mean:Double, sd: Double, totalNeighbours: Int, sumAllNeighboursPoints: Int) => ((HotcellUtils.GetisOrd(x, y, z, numcells, mean, sd, totalNeighbours, sumAllNeighboursPoints))))
    val NeighboursDesc = spark.sql("select x, y, z, GetisOrd(x, y, z," +numCells+ ", " + Xbar + ", " + SD + ", totalNeighbours, sumAllNeighboursPoints) as gi_statistic from NeighboursView order by gi_statistic desc")
    NeighboursDesc.createOrReplaceTempView("NeighboursDescView")
    val results = spark.sql("select x,y,z from NeighboursDescView")

    return results
  }
}
