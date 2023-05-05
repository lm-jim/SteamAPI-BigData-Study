import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, Uri}
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

object AkkaTests{

  case class GameInfo(AppID: Int,
                      productType: String,
                      productName: String,
                      requiredAge: Option[Int],
                      controllerSupport: Option[String],
                      dlc: Option[List[Int]],
                      shortDescription: Option[String],

                      fullgame_appid: Option[Int],
                      fullgame_appname: Option[String],
                      supportedLanguages: Option[String],
                      headerImage: Option[String],
                      website: Option[String],

                      pcRequirementsMin: Option[String],
                      pcRequirementsRecommended: Option[String],
                      developers: Option[List[String]],
                      publishers: List[String],

                      demo: Option[String],
                      price: Option[String],

                      windowsCompatible: Option[Boolean],
                      macCompatible: Option[Boolean],
                      linuxCompatible: Option[Boolean],

                      metacriticScore: Option[Int],
                      achievementCount: Option[Int],
                      releaseDate: Option[String],
                      drm: Option[String],

                      categories: List[String],
                      genres: List[String]


                     )

  def sendRequest(PID: Int)(implicit sys: ActorSystem): Future[GameInfo] ={

    import sys.dispatcher

    val URI = "https://store.steampowered.com/api/appdetails"
    val param = Map("appids" -> PID.toString)
    val request = HttpRequest(HttpMethods.POST, Uri(URI).withQuery(Query(param)))

    val response = Http().singleRequest(request)
    val entityFuture = response.flatMap(x => x.entity.toStrict(5.seconds))

    val obtainedResponse = entityFuture.map(_.data.utf8String)

    //obtainedResponse.map(println)


    val JSON_parse = obtainedResponse.map(Json.parse(_))

    val JSON_productType = JSON_parse.map(x => (x \ PID.toString \ "data" \ "type").as[String])
    val JSON_name = JSON_parse.map(x => (x \ PID.toString \ "data" \ "name").as[String])
    val JSON_requiredAge = JSON_parse.map(x => (x \ PID.toString \ "data" \ "required_age").asOpt[Int])
    val JSON_controllerSupport = JSON_parse.map(x => (x \ PID.toString \ "data" \ "controller_support").asOpt[String])
    val JSON_dlc = JSON_parse.map(x => (x \ PID.toString \ "data" \ "dlc").asOpt[List[Int]])
    val JSON_shortDesc = JSON_parse.map(x => (x \ PID.toString \ "data" \ "short_description").asOpt[String])

    val JSON_fullgameAppid = JSON_parse.map(x => (x \ PID.toString \ "data" \ "fullgame" \ "appid").asOpt[Int])
    val JSON_fullgameAppname = JSON_parse.map(x => (x \ PID.toString \ "data" \ "fullgame" \ "name").asOpt[String])
    val JSON_supportedLanguages = JSON_parse.map(x => (x \ PID.toString \ "data" \ "supported_languages").asOpt[String])
    val JSON_headerImage = JSON_parse.map(x => (x \ PID.toString \ "data" \ "header_image").asOpt[String])
    val JSON_website = JSON_parse.map(x => (x \ PID.toString \ "data" \ "website").asOpt[String])

    val JSON_pcReqMin = JSON_parse.map(x => (x \ PID.toString \ "data" \ "pc_requirements" \ "minimum").asOpt[String])
    val JSON_pcReqRec = JSON_parse.map(x => (x \ PID.toString \ "data" \ "pc_requirements" \ "recommended").asOpt[String])
    val JSON_developers = JSON_parse.map(x => (x \ PID.toString \ "data" \ "developers").asOpt[List[String]])
    val JSON_publishers = JSON_parse.map(x => (x \ PID.toString \ "data" \ "publishers").as[List[String]])

    val JSON_demo = JSON_parse.map(x => (x \ PID.toString \ "data" \ "demos" \ "appid").asOpt[String])
    val JSON_price = JSON_parse.map(x => (x \ PID.toString \ "data" \ "price_overview" \ "final_formatted").asOpt[String])

    val JSON_windowsComp = JSON_parse.map(x => (x \ PID.toString \ "data" \ "platforms" \ "windows").asOpt[Boolean])
    val JSON_macComp = JSON_parse.map(x => (x \ PID.toString \ "data" \ "platforms" \ "mac").asOpt[Boolean])
    val JSON_linuxComp = JSON_parse.map(x => (x \ PID.toString \ "data" \ "platforms" \ "linux").asOpt[Boolean])

    val JSON_metacriticScore = JSON_parse.map(x => (x \ PID.toString \ "data" \ "metacritic" \ "score").asOpt[Int])
    val JSON_achievementCount = JSON_parse.map(x => (x \ PID.toString \ "data" \ "achievements" \ "total").asOpt[Int])
    val JSON_releaseDate = JSON_parse.map(x => (x \ PID.toString \ "data" \ "release_date" \ "date").asOpt[String])
    val JSON_drm = JSON_parse.map(x => (x \ PID.toString \ "data" \ "drm_notice").asOpt[String])

    val JSON_categories = JSON_parse.map(x => (x \ PID.toString \ "data" \ "categories" \\ "description"))
    val JSON_genres = JSON_parse.map(x => (x \ PID.toString \ "data" \ "genres" \\ "description"))


/*
    JSON_parse.recover(x => println("JSON_parse error " + x))
    JSON_name.recover(x => println("JSON_name error " + x))
    JSON_requiredAge.recover(x => println("JSON_requiredAge error " + x))
    JSON_controllerSupport.recover(x => println("JSON_controllerSupport error " + x))
    JSON_dlc.recover(x => println("JSON_dlc error " + x))
    JSON_shortDesc.recover(x => println("JSON_shortDesc error " + x))
    JSON_fullgameAppid.recover(x => println("JSON_fullgameAppid error " + x))
    JSON_fullgameAppname.recover(x => println("JSON_fullgameAppname error " + x))
    JSON_supportedLanguages.recover(x => println("JSON_supportedLanguages error " + x))
    JSON_headerImage.recover(x => println("JSON_headerImage error " + x))
    JSON_website.recover(x => println("JSON_website error " + x))

    JSON_developers.recover(x => println("JSON_developers error " + x))
    JSON_publishers.recover(x => println("JSON_publishers error " + x))*/

    for{
      productType <- JSON_productType
      name <- JSON_name
      requiredAge <- JSON_requiredAge
      controllerSupport <- JSON_controllerSupport
      dlc <- JSON_dlc
      shortDesc <- JSON_shortDesc

      fullGameAppID <- JSON_fullgameAppid
      fullGameAppName <- JSON_fullgameAppname
      supportedLanguages <- JSON_supportedLanguages
      headerImg <- JSON_headerImage
      website <- JSON_website

      pcReqMin <- JSON_pcReqMin
      pcReqRec <- JSON_pcReqRec
      developers <- JSON_developers
      publishers <- JSON_publishers

      demo <- JSON_demo
      price <- JSON_price

      windowsComp <- JSON_windowsComp
      macComp <- JSON_macComp
      linuxComp <- JSON_linuxComp

      metacriticScore <- JSON_metacriticScore
      achievementCount <- JSON_achievementCount
      releaseDate <- JSON_releaseDate
      drm <- JSON_drm

      categories <- JSON_categories
      genres <- JSON_genres

    } yield GameInfo(PID,
      productType,
      name,
      requiredAge,
      controllerSupport,
      dlc,
      shortDesc,

      fullGameAppID,
      fullGameAppName,
      supportedLanguages,
      headerImg,
      website,

      pcReqMin,
      pcReqRec,
      developers,
      publishers,

      demo,
      price,

      windowsComp,
      macComp,
      linuxComp,

      metacriticScore,
      achievementCount,
      releaseDate,
      drm,

      categories.map(_.as[String]).toList,
      genres.map(_.as[String]).toList

    )

  }



}
