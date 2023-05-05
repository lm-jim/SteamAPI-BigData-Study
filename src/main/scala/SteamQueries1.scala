import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object SteamQueries1 extends App {

  val spark = SparkSession.builder().master("local").getOrCreate();

  spark.sparkContext.setLogLevel("ERROR")

  val dataDir = "SteamAPIData/Daily"
  val mySchema = StructType(List(
    StructField("AppID", IntegerType, true),
    StructField("ProductType", StringType, true),
    StructField("ProductName", StringType, true),
    StructField("RequiredAge", StringType, true),
    StructField("ControllerSupport", StringType, true),
    StructField("Dlc", StringType, true),
    StructField("ShortDescription", StringType, true),
    StructField("FullGameAppID", StringType, true),
    StructField("FullGameName", StringType, true),
    StructField("SupportedLanguages", StringType, true),
    StructField("HeaderImage", StringType, true),
    StructField("Website", StringType, true),
    StructField("PcRequirementsMin", StringType, true),
    StructField("PcRequirementsMax", StringType, true),
    StructField("Developers", StringType, true),
    StructField("Publishers", StringType, true),
    StructField("Demo", StringType, true),
    StructField("Price", StringType, true),
    StructField("WindowsCompatible", StringType, true),
    StructField("MacCompatible", StringType, true),
    StructField("LinuxCompatible", StringType, true),
    StructField("MetacriticScore", StringType, true),
    StructField("AchievementCount", StringType, true),
    StructField("ReleaseDate", StringType, true),
    StructField("DRM", StringType, true),
    StructField("Categories", StringType, true),
    StructField("Genres", StringType, true)
  ))


  val steamGames_df = spark.read.schema(mySchema).option("header", "true").csv(dataDir);

  val steamGames_df_formattedDate = steamGames_df.select(col("ReleaseDate"),
    when(to_date(col("ReleaseDate"), "d MMM, yyyy").isNotNull, to_date(col("ReleaseDate"), "d MMM, yyyy"))
      .when(to_date(col("ReleaseDate"), "M yyyy").isNotNull, to_date(col("ReleaseDate"), "M yyyy"))
  ).as("ReleaseDateFormatted")

  print(steamGames_df_formattedDate.count() + " games")
  print(steamGames_df_formattedDate.schema)
  steamGames_df_formattedDate.orderBy(desc("AppID")).show(50)

}
