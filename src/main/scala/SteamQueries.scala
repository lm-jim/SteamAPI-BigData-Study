import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object SteamQueries extends App {

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

  implicit class DfExtension(val df: DataFrame){
    def saveToCsv(path: String) = {
      df.write
        .format("com.databricks.spark.csv")
        .mode("overwrite")
        .save(s"OutputCSVs/$path")
    }
  }

  val steamGames_df = spark.read.schema(mySchema).option("header", "true").csv(dataDir);
  val convertedDF = steamGames_df.withColumn("ReleaseDate", to_date(col("ReleaseDate"), "d MMM, yyyy"))

  convertedDF.createOrReplaceTempView("steam_df")

  ////////////////////////////////
  ///////  MINICONSULTAS /////////
  ////////////////////////////////

  //Porcentaje de juegos con soporte de mando
  val controllerSupportQuery = spark.sql(
    s"""SELECT ControllerSupport, COUNT(*) * 100 / CAST(SUM(COUNT(*)) OVER () AS float)
                FROM steam_df
                GROUP BY ControllerSupport""")
  controllerSupportQuery.saveToCsv("GamesWithControllerSupport")

  //Porcentaje de juegos con algún tipo de DRM presente
  val drmQuery = spark.sql(
    s"""SELECT DRM, COUNT(*) * 100 / CAST(SUM(COUNT(*)) OVER () AS float) AS DRM_Percentage
                FROM steam_df
                GROUP BY DRM
                ORDER BY DRM_Percentage DESC""")
  drmQuery.saveToCsv("GamesWithDRM")

  ////////////////////////////////////////
  ///////  CONSULTAS PRINCIPALES /////////
  ////////////////////////////////////////

  //Número de juegos lanzados en Steam por fecha
  val gamesReleasedByDateQuery = spark.sql(
    s"""SELECT ReleaseDate, COUNT(*) AS GamesReleased
                FROM steam_df
                GROUP BY ReleaseDate
                ORDER BY ReleaseDate""")
  gamesReleasedByDateQuery.saveToCsv("ReleaseDateCount")

  //Distribución de precios de los juegos
  val gamePriceDistributionQuery = spark.sql(
    s"""SELECT CAST(REGEXP_REPLACE(REGEXP_REPLACE(Price, '[$$€]', ''), ',', '.') AS float) AS PriceFloat,
                      COUNT(*) as GamesPrice
                FROM steam_df
                GROUP BY PriceFloat
                ORDER BY PriceFloat""")
  gamePriceDistributionQuery.saveToCsv("GamePriceDistribution")

  //Número de cada tipo de producto en la tienda de Steam
  val productTypeCountQuery = spark.sql(
    s"""SELECT ProductType, COUNT(*) AS Count
                  FROM steam_df
                  GROUP BY ProductType
                  ORDER BY Count DESC""")
  productTypeCountQuery.saveToCsv("ProductTypeCount")

  //Compatibilidad con sistemas operativos
  val osCompatibilityQuery = spark.sql(
    s"""SELECT WindowsCompatible, MacCompatible, LinuxCompatible,
                        COUNT(*) AS CompatibleGames
                  FROM steam_df
                  GROUP BY WindowsCompatible, MacCompatible, LinuxCompatible
                  ORDER BY CompatibleGames DESC""")
  osCompatibilityQuery.saveToCsv("CompatibleGames")

  //Categorías más comunes
  val categoryQuery = spark.sql(
    s"""SELECT Category, COUNT(*) AS GameCount
                  FROM (
                        SELECT TRIM(Category) AS Category
                        FROM steam_df
                        LATERAL VIEW explode(split(Categories, ',')) AS Category
                  )
                  GROUP BY Category
                  ORDER BY GameCount DESC""")
  categoryQuery.saveToCsv("CategoryCount")

  //Géneros más comunes
  val genreQuery = spark.sql(
    s"""SELECT Genre, COUNT(*) AS GameCount
                  FROM (
                        SELECT TRIM(Genre) AS Genre
                        FROM steam_df
                        LATERAL VIEW explode(split(Genres, ',')) AS Genre
                  )
                  GROUP BY Genre
                  ORDER BY GameCount DESC""")
  genreQuery.saveToCsv("GenreCount")

  //De esta última consulta, hemos obtenido que los géneros más comunes en Steam son:
  //"Indie", "Acción", "Casual", "Aventura", "Simulación", "RPG", "Estrategsuia" y "Deportes" en ese orden.
  //Sería interesante comparar el precio medio cada uno de estos géneros y  mediana

    List("Avg", "Median").foreach(x =>
    spark.sql(
    s"""SELECT Genre, """ + (if (x eq "Avg") "AVG(PriceFloat)" else "percentile(PriceFloat, 0.5)") + s""" AS GenrePrice$x
                FROM (
                      SELECT TRIM(Genre) AS Genre,
                            CAST(REGEXP_REPLACE(REGEXP_REPLACE(Price, '[$$€]', ''), ',', '.') AS float) AS PriceFloat
                      FROM steam_df
                      LATERAL VIEW explode(split(Genres, ',')) AS Genre
                )
                WHERE Genre IN ('Indie', 'Action', 'Casual', 'Adventure', 'Simulation', 'RPG', 'Strategy', 'Sports')
                GROUP BY Genre
                ORDER BY GenrePrice$x DESC"""
    )
    .saveToCsv(s"GenrePrice$x")
  )

  //Parece que cuanto más popular es el género, menor precio suelen tener
  //Hagamos lo mismo con las categorías más comunes

  List("Avg", "Median").foreach(x =>
    spark.sql(
      s"""SELECT Category, """ + (if (x eq "Avg") "AVG(PriceFloat)" else "percentile(PriceFloat, 0.5)") + s""" AS CategoryPrice$x
              FROM (
                    SELECT TRIM(Category) AS Category,
                          CAST(REGEXP_REPLACE(REGEXP_REPLACE(Price, '[$$€]', ''), ',', '.') AS float) AS PriceFloat
                    FROM steam_df
                    LATERAL VIEW explode(split(Categories, ',')) AS Category
              )
              WHERE Category IN ('Single-player', 'Steam Achievements', 'Downloadable Content', 'Steam Cloud', 'Full controller support', 'Multi-player')
              GROUP BY Category
              ORDER BY CategoryPrice$x DESC"""
    )
    .saveToCsv(s"CategoryPrice$x")
  )

  //Por último, vamos a calcular el precio más común en cada género y caregoría

  val mostFrequentGenrePriceQuery = spark.sql(
    s"""WITH RankedValues AS (
                      SELECT Genre, PriceFloat,
                            ROW_NUMBER() OVER (PARTITION BY Genre ORDER BY COUNT(PriceFloat) DESC) AS rank
                      FROM (
                            SELECT TRIM(Genre) AS Genre,
                                  CAST(REGEXP_REPLACE(REGEXP_REPLACE(Price, '[$$€]', ''), ',', '.') AS float) AS PriceFloat
                            FROM steam_df
                            LATERAL VIEW explode(split(Genres, ',')) AS Genre
                      )
                      GROUP BY Genre, PriceFloat
                )
                SELECT Genre, PriceFloat
                FROM RankedValues
                WHERE rank = 1
                ORDER BY PriceFloat DESC
       """
  )
  mostFrequentGenrePriceQuery.saveToCsv("GenrePriceMostFrequent")

  val mostFrequentCategoryPriceQuery = spark.sql(
    s"""WITH RankedValues AS (
                      SELECT Category, PriceFloat,
                            ROW_NUMBER() OVER (PARTITION BY Category ORDER BY COUNT(PriceFloat) DESC) AS rank
                      FROM (
                            SELECT TRIM(Category) AS Category,
                                  CAST(REGEXP_REPLACE(REGEXP_REPLACE(Price, '[$$€]', ''), ',', '.') AS float) AS PriceFloat
                            FROM steam_df
                            LATERAL VIEW explode(split(Categories, ',')) AS Category
                      )
                      GROUP BY Category, PriceFloat
                )
                SELECT Category, PriceFloat
                FROM RankedValues
                WHERE rank = 1
                ORDER BY PriceFloat DESC
       """
  )
  mostFrequentCategoryPriceQuery.saveToCsv("CategoryPriceMostFrequent")

}

