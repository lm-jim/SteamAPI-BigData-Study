import akka.actor.ActorSystem
import play.api.libs.json.Json

import java.io.FileInputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import scala.io.Source
import scala.reflect.io.File

object CsvSaveManager extends App{

  implicit val system = ActorSystem()
  implicit val localDateOrdering: Ordering[LocalDate] = Ordering.by(_.toEpochDay)

  val todaysDate = java.time.LocalDate.now
  val lastDate = {
    val files = new java.io.File("SteamAPIData/Daily/").listFiles()
    val dateFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd")

    files.map(x => x.getName.substring(12, x.getName.length - 4))
      .map(LocalDate.parse(_, dateFormat)).max
  }

  val NUMBER_OF_PETITIONS = 12000

  if(!File("SteamAPIData/Daily/steamOutput_" + todaysDate + ".csv").exists){

    val fw = File("SteamAPIData/Daily/steamOutput_" + todaysDate + ".csv").createFile()
    fw.writeAll("")
    fw.appendAll("AppID,ProductType,ProductName,RequiredAge,ControllerSupport,Dlc,ShortDescription,FullGameAppID,FullGameName,SupportedLanguages,HeaderImage,Website,PcRequirementsMin,PcRequirementsMax,Developers,Publishers,Demo,Price,WindowsCompatible,MacCompatible,LinuxCompatible,MetacriticScore,AchievementCount,ReleaseDate,DRM,Categories,Genres\n")

    println("Calculating today's petitions")
    val steamAllAppsJSON = Json.parse(new FileInputStream("SteamAPIData/steamAllApps.json"))
    val lastGameID = getYesterdaysLastCsvGameID

    val allPetitions = (steamAllAppsJSON \\ "appid").map(_.as[Int]).toList
    val remainingPetitions = allPetitions.sorted.filter(_ > lastGameID)
    val todaysPetitions = remainingPetitions.slice(0, NUMBER_OF_PETITIONS)

    val remainingPercentage = (1 - remainingPetitions.length.toFloat / allPetitions.length.toFloat) * 100

    println(f"Calculated today's petitions. Total progress: $remainingPercentage%.2f%%")

    dailySteamPetitions(fw, todaysPetitions)
  }
  else{
    println("Today's .csv already exists")
    System.exit(0)
  }

  implicit class CsvStringUtils(val s: String){
    def quotationMarked = "\"" + s.replace("\"","'") + "\""
    def formatHTML = s.filter(_ >= ' ').replaceAll("<[^>]*>", "")
  }

  def getYesterdaysLastCsvGameID = {
    if(File("SteamAPIData/Daily/steamOutput_" + lastDate + ".csv").exists){
      val lines = Source.fromFile("SteamAPIData/Daily/steamOutput_" + lastDate + ".csv").getLines()
      val lastLine = lines.foldLeft(Option.empty[String]) { case (_, line) => Some(line) }

      lastLine.map(_.split(",")) match {
        case Some(intArray) => intArray(0).toInt
        case None => 0
      }
    }
    else 0
  }

  def listToCsv(list: List[Any]): String = list match {
    case List() => ""
    case h :: List() => h.toString
    case h :: tail => h.toString + ", " + listToCsv(tail)
  }

  def optionToCsv(op: Option[Any]): String = op match {
    case Some(op) => op match {
      case op: List[Any] => listToCsv(op)
      case _ => op.toString
    }
    case None => ""
  }

  def dailySteamPetitions(fw: File, petitions: List[Int]) = {

    //120 requests per minute
    //2,400 requests per hour
    //14,400 requests per day

    //13,320 por dia => 6 horas => 2220 req/hora = 37 req/min
    for (i <- petitions) {

      Thread.sleep(1400)

      val petition = AkkaSteamPetition.sendRequest(i)
      val completedPercentage = (petitions.indexOf(i).toFloat / petitions.length.toFloat) * 100

      print(f"[$completedPercentage%.2f%%] Sending petition for appid=" + i + " Result: ")

      try {
        val x = Await.result(petition, 5.second)

        fw.appendAll(x.AppID + ","
          + x.productType + ","
          + x.productName.quotationMarked + ","
          + optionToCsv(x.requiredAge) + ","
          + optionToCsv(x.controllerSupport) + ","
          + optionToCsv(x.dlc).quotationMarked + ","
          + optionToCsv(x.shortDescription).quotationMarked + ","

          + optionToCsv(x.fullgame_appid) + ","
          + optionToCsv(x.fullgame_appname).quotationMarked + ","
          + optionToCsv(x.supportedLanguages).formatHTML.quotationMarked + ","
          + optionToCsv(x.headerImage) + ","
          + optionToCsv(x.website) + ","

          + optionToCsv(x.pcRequirementsMin).formatHTML.quotationMarked + ","
          + optionToCsv(x.pcRequirementsRecommended).formatHTML.quotationMarked + ","
          + optionToCsv(x.developers).quotationMarked + ","
          + listToCsv(x.publishers).quotationMarked + ","

          + optionToCsv(x.demo) + ","
          + optionToCsv(x.price).quotationMarked + ","

          + optionToCsv(x.windowsCompatible) + ","
          + optionToCsv(x.macCompatible) + ","
          + optionToCsv(x.linuxCompatible) + ","

          + optionToCsv(x.metacriticScore) + ","
          + optionToCsv(x.achievementCount) + ","
          + optionToCsv(x.releaseDate).quotationMarked + ","
          + optionToCsv(x.drm) + ","

          + listToCsv(x.categories).quotationMarked + ","
          + listToCsv(x.genres).quotationMarked

          + "\n")

        println(" Completed successfully!")
      }
      catch {
        case ex: Exception => println(" Caused an exception!!: [" + ex + "]")
      }
    }
    println("------------------------------------------------------------------------------")
    println("Finished today's .csv       Total petitions: " + petitions.length)
    System.exit(0)
  }

}
