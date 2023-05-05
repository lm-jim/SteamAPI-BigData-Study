import org.apache.spark.sql._
import org.apache.spark.sql.types._
import org.apache.spark.sql.functions._

object SparkTest extends App {

  val spark = SparkSession.builder().master("local").getOrCreate();

  spark.sparkContext.setLogLevel("ERROR")

  val dataDir = "SteamAPIData/ccaa_covid19_casos_long.csv/"

  val mySchema = StructType(List(StructField("fecha", TimestampType, true),
    StructField("cod_ine", IntegerType, true),
    StructField("CCAA", StringType, true),
    StructField("total", LongType, true)))


  val df = spark.read.schema(mySchema).options(Map("header" -> "true")).csv(dataDir);
  //df.groupBy("CCAA").sum("total").select("CCAA", "sum(total)").withColumnRenamed("sum(total)","Total Infectados").orderBy("Total Infectados").show(50)
  val dfForPlot = df.groupBy("CCAA").agg(max("total")).orderBy("max(total)").withColumnRenamed("max(total)", "Total casos")
  dfForPlot.show(50)

}
