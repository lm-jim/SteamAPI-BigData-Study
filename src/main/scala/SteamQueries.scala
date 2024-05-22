import org.apache.spark.sql._
import org.apache.spark.sql.expressions.Window
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

  //Valores para la generación de resultados:
  val USE_SQL = true;        //Usar consultas directamente SQL
  val USE_COL_EXP = true;    //Usar expresiones de columnas de Spark

  ////////////////////////////////
  ///////  MINICONSULTAS /////////
  ////////////////////////////////

  //Porcentaje de juegos con soporte de mando
  if(USE_SQL) {
    val controllerSupportQuery = spark.sql(
      s"""SELECT ControllerSupport, COUNT(*) * 100 / CAST(SUM(COUNT(*)) OVER () AS float)
                FROM steam_df
                GROUP BY ControllerSupport""")

    controllerSupportQuery.saveToCsv("SQL/GamesWithControllerSupport")
  }
  if(USE_COL_EXP) {
    val controllerSuppCountDf = convertedDF.groupBy("ControllerSupport").count()
    val controllerSupportQuery = controllerSuppCountDf
      .withColumn("percentage", col("count") * 100 / convertedDF.count())
      .select("ControllerSupport", "percentage")
      .orderBy(desc("percentage"))

    controllerSupportQuery.saveToCsv("NonSQL/GamesWithControllerSupport")
  }

  //Porcentaje de juegos con algún tipo de DRM presente
  if(USE_SQL) {
    val drmQuery = spark.sql(
      s"""SELECT DRM, COUNT(*) * 100 / CAST(SUM(COUNT(*)) OVER () AS float) AS DRM_Percentage
                FROM steam_df
                GROUP BY DRM
                ORDER BY DRM_Percentage DESC""")

    drmQuery.saveToCsv("SQL/GamesWithDRM")
  }
  if(USE_COL_EXP) {
    val drmCountDf = convertedDF.groupBy("DRM").count()
    val drmQuery = drmCountDf
      .withColumn("percentage", col("count") * 100 / convertedDF.count())
      .select("DRM", "percentage")
      .orderBy(desc("percentage"))

    drmQuery.saveToCsv("NonSQL/GamesWithDRM")
  }

  ////////////////////////////////////////
  ///////  CONSULTAS PRINCIPALES /////////
  ////////////////////////////////////////


  //Número de juegos lanzados en Steam por fecha
  if(USE_SQL){
    val gamesReleasedByDateQuery = spark.sql(
      s"""SELECT ReleaseDate, COUNT(*) AS GamesReleased
                  FROM steam_df
                  GROUP BY ReleaseDate
                  ORDER BY ReleaseDate""")

    gamesReleasedByDateQuery.saveToCsv("SQL/ReleaseDateCount")
  }
  if(USE_COL_EXP){
    val gamesReleasedByDateQuery = convertedDF.groupBy("ReleaseDate").count().orderBy("ReleaseDate")

    gamesReleasedByDateQuery.saveToCsv("NonSQL/ReleaseDateCount")
  }

  //Distribución de precios de los juegos
  if(USE_SQL){
    val gamePriceDistributionQuery = spark.sql(
      s"""SELECT CAST(REGEXP_REPLACE(REGEXP_REPLACE(Price, '[$$€]', ''), ',', '.') AS float) AS PriceFloat,
                      COUNT(*) as GamesPrice
                FROM steam_df
                GROUP BY PriceFloat
                ORDER BY PriceFloat""")

    gamePriceDistributionQuery.saveToCsv("SQL/GamePriceDistribution")
  }
  if(USE_COL_EXP){
    val regexpPriceDf = convertedDF.withColumn("PriceFloat", regexp_replace(col("Price"), "[$€]", ""))
      .withColumn("PriceFloat", regexp_replace(col("PriceFloat"), ",", ".").cast("float"))
    val gamePriceDistributionQuery = regexpPriceDf.groupBy("PriceFloat").count().orderBy("PriceFloat")

    gamePriceDistributionQuery.saveToCsv("NonSQL/GamePriceDistribution")
  }

  //Número de cada tipo de producto en la tienda de Steam
  if(USE_SQL){
    val productTypeCountQuery = spark.sql(
      s"""SELECT ProductType, COUNT(*) AS Count
                  FROM steam_df
                  GROUP BY ProductType
                  ORDER BY Count DESC""")

    productTypeCountQuery.saveToCsv("SQL/ProductTypeCount")
  }
  if(USE_COL_EXP){
    val productTypeCountQuery = convertedDF.groupBy("ProductType").count().orderBy(desc("count"))

    productTypeCountQuery.saveToCsv("NonSQL/ProductTypeCount")
  }

  //Compatibilidad con sistemas operativos
  if(USE_SQL){
    val osCompatibilityQuery = spark.sql(
      s"""SELECT WindowsCompatible, MacCompatible, LinuxCompatible,
                        COUNT(*) AS CompatibleGames
                  FROM steam_df
                  GROUP BY WindowsCompatible, MacCompatible, LinuxCompatible
                  ORDER BY CompatibleGames DESC""")

    osCompatibilityQuery.saveToCsv("SQL/CompatibleGames")
  }
  if(USE_COL_EXP){
    val osCompatibilityQuery = convertedDF.groupBy("WindowsCompatible", "MacCompatible", "LinuxCompatible").count()
      .orderBy(desc("count"))

    osCompatibilityQuery.saveToCsv("NonSQL/CompatibleGames")
  }

  //Categorías más comunes
  if(USE_SQL){
    val categoryQuery = spark.sql(
      s"""SELECT Category, COUNT(*) AS GameCount
                  FROM (
                        SELECT TRIM(Category) AS Category
                        FROM steam_df
                        LATERAL VIEW explode(split(Categories, ',')) AS Category
                  )
                  GROUP BY Category
                  ORDER BY GameCount DESC""")

    categoryQuery.saveToCsv("SQL/CategoryCount")
  }
  if(USE_COL_EXP){
    val splittedCategoriesDf = convertedDF.withColumn("Category", explode(split(col("Categories"), ",")))
    val categoryQuery = splittedCategoriesDf.withColumn("Category", trim(col("Category"))).groupBy("Category").count()
      .orderBy(desc("count"))

    categoryQuery.saveToCsv("NonSQL/CategoryCount")
  }

  //Géneros más comunes
  if(USE_SQL){
    val genreQuery = spark.sql(
      s"""SELECT Genre, COUNT(*) AS GameCount
                  FROM (
                        SELECT TRIM(Genre) AS Genre
                        FROM steam_df
                        LATERAL VIEW explode(split(Genres, ',')) AS Genre
                  )
                  GROUP BY Genre
                  ORDER BY GameCount DESC""")

    genreQuery.saveToCsv("SQL/GenreCount")
  }
  if(USE_COL_EXP){
    val splittedGenresDf = convertedDF.withColumn("Genre", explode(split(col("Genres"), ",")))
    val genreQuery = splittedGenresDf.withColumn("Genre", trim(col("Genre"))).groupBy("Genre").count()
      .orderBy(desc("count"))

    genreQuery.saveToCsv("NonSQL/GenreCount")
  }

  //De esta última consulta, hemos obtenido que los géneros más comunes en Steam son:
  //"Indie", "Acción", "Casual", "Aventura", "Simulación", "RPG", "Estrategsuia" y "Deportes" en ese orden.
  //Sería interesante comparar el precio medio cada uno de estos géneros y  mediana

    List("Avg", "Median").foreach(x => {
      if(USE_SQL){
        spark.sql(
          s"""SELECT Genre, """ + (if (x eq "Avg") "AVG(PriceFloat)" else "percentile(PriceFloat, 0.5)") +
            s""" AS GenrePrice$x
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
          .saveToCsv(s"SQL/GenrePrice$x")
      }
      if(USE_COL_EXP){
        val regexpPriceDf = convertedDF
          .withColumn("PriceFloat", regexp_replace(col("Price"), "[$€]", ""))
          .withColumn("PriceFloat", regexp_replace(col("PriceFloat"), ",", ".").cast("float"))

        val trimmedGenreNPrice = regexpPriceDf
          .withColumn("Genre", explode(split(col("Genres"), ",")))
          .withColumn("Genre", trim(col("Genre")))
          .select("Genre", "PriceFloat")

        trimmedGenreNPrice.groupBy("Genre")
          .agg({if (x eq "Avg") avg("PriceFloat") else expr("approx_percentile(PriceFloat, 0.5)")}.as(s"GenrePrice$x"))
          .filter(col("Genre")
            .isin("Indie", "Action", "Casual", "Adventure", "Simulation", "RPG", "Strategy", "Sports"))
          .orderBy(desc(s"GenrePrice$x"))
          .saveToCsv(s"NonSQL/GenrePrice$x")
      }
    }
  )

  //Parece que cuanto más popular es el género, menor precio suelen tener
  //Hagamos lo mismo con las categorías más comunes

  List("Avg", "Median").foreach(x => {
    if(USE_SQL){
      spark.sql(
        s"""SELECT Category, """ + (if (x eq "Avg") "AVG(PriceFloat)" else "percentile(PriceFloat, 0.5)") +
          s""" AS CategoryPrice$x
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
        .saveToCsv(s"SQL/CategoryPrice$x")
    }
    if (USE_COL_EXP) {
      val regexpPriceDf = convertedDF
        .withColumn("PriceFloat", regexp_replace(col("Price"), "[$€]", ""))
        .withColumn("PriceFloat", regexp_replace(col("PriceFloat"), ",", ".").cast("float"))

      val trimmedCategoryNPrice = regexpPriceDf
        .withColumn("Category", explode(split(col("Categories"), ",")))
        .withColumn("Category", trim(col("Category")))
        .select("Category", "PriceFloat")

      trimmedCategoryNPrice.groupBy("Category")
        .agg({if (x eq "Avg") avg("PriceFloat") else expr("approx_percentile(PriceFloat, 0.5)")}.as(s"CategoryPrice$x"))
        .filter(col("Category")
          .isin("Single-player", "Steam Achievements", "Downloadable Content", "Steam Cloud", "Full controller support", "Multi-player"))
        .orderBy(desc(s"CategoryPrice$x"))
        .saveToCsv(s"NonSQL/CategoryPrice$x")
      }
    }
  )

  //Por último, vamos a calcular el precio más común en cada género y caregoría

  if(USE_SQL){
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
       """)

    mostFrequentGenrePriceQuery.saveToCsv("SQL/GenrePriceMostFrequent")
  }
  if(USE_COL_EXP){
    val regexpPriceDf = convertedDF
      .withColumn("PriceFloat", regexp_replace(col("Price"), "[$€]", ""))
      .withColumn("PriceFloat", regexp_replace(col("PriceFloat"), ",", ".").cast("float"))

    val trimmedGenreNPrice = regexpPriceDf
      .withColumn("Genre", explode(split(col("Genres"), ",")))
      .withColumn("Genre", trim(col("Genre")))
      .select("Genre", "PriceFloat")

    val partitions = Window.partitionBy("Genre").orderBy(desc("PriceCount"))

    val mostFrequentGenrePriceQuery = trimmedGenreNPrice
      .groupBy("Genre", "PriceFloat")
      .agg(count("PriceFloat").as("PriceCount"))
      .withColumn("rank", row_number().over(partitions))
      .filter(col("rank") === 1)
      .drop("rank", "PriceCount")
      .orderBy(desc("PriceFloat"))

    mostFrequentGenrePriceQuery.saveToCsv("NonSQL/GenrePriceMostFrequent")
  }

  if(USE_SQL){
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

    mostFrequentCategoryPriceQuery.saveToCsv("SQL/CategoryPriceMostFrequent")
  }
  if (USE_COL_EXP) {
    val regexpPriceDf = convertedDF
      .withColumn("PriceFloat", regexp_replace(col("Price"), "[$€]", ""))
      .withColumn("PriceFloat", regexp_replace(col("PriceFloat"), ",", ".").cast("float"))

    val trimmedCategoryNPrice = regexpPriceDf
      .withColumn("Category", explode(split(col("Categories"), ",")))
      .withColumn("Category", trim(col("Category")))
      .select("Category", "PriceFloat")

    val partitions = Window.partitionBy("Category").orderBy(desc("PriceCount"))

    val mostFrequentGenrePriceQuery = trimmedCategoryNPrice
      .groupBy("Category", "PriceFloat")
      .agg(count("PriceFloat").as("PriceCount"))
      .withColumn("rank", row_number().over(partitions))
      .filter(col("rank") === 1)
      .drop("rank", "PriceCount")
      .orderBy(desc("PriceFloat"))

    mostFrequentGenrePriceQuery.saveToCsv("NonSQL/CategoryPriceMostFrequent")
  }

}

