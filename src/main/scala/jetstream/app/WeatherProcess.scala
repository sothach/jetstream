package jetstream.app

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.Host
import akka.stream.Supervision.{Decider, Resume}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import jetstream.model.weather._
import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json._

import scala.collection.immutable
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object WeatherProcess extends App {

  /*
 http://api.openweathermap.org/pollution/v1/co/0.0,10.0/current.json?appid=dabb604404d141ed6e8af1936881b5d8
 http://erc.epa.ie/real-time-air/www/aqindex/aqih_json.php
 http://erc.epa.ie/services/json/safer_recent.json
 https://world.openfoodfacts.org/api/v0/product/5000157062680.json (additives)
 select * from collection_item where code in (
 '5000157062680',
 '5010046001102',
 '3254474011766',
 '5010046005001',
 '0014100097006',
 '7622210286956',
 '5011476004411',
 '5011476100069',
 '5053990138661');
   */
  implicit val config = JsonConfiguration(SnakeCase)
  object WeatherJsonProtocol {
    implicit val formatCoordinate = Json.format[Coordinate]
    implicit val formatWeather = Json.format[Weather]
    implicit val formatMain = Json.format[Main]
    implicit val formatWind = Json.format[Wind]
    implicit val formatClouds = Json.format[Clouds]
    implicit val formatSys = Json.format[Sys]
    implicit val weatherFormat = Json.format[Response]
  }
  import WeatherJsonProtocol._
  implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()


  implicit val system = ActorSystem("weather-system")
  implicit val ec = system.dispatcher
  val decider: Decider = { ex =>
    println(s"ProcessExample resuming because of ${ex.getClass.getCanonicalName} (${ex.getMessage})")
    ex.printStackTrace()
    Resume
  }
  implicit val materializer = ActorMaterializer(
    ActorMaterializerSettings(system).withSupervisionStrategy(decider))(system)

  val appId = "dabb604404d141ed6e8af1936881b5d8"
  val endpoint = Uri(s"http://api.openweathermap.org/data/2.5/weather")

  val source = Source(immutable.Seq("Dublin,ie","London,uk","Maynooth,ie","Munich,de"))

  val buildRequest = Flow[String].map { ort =>
    val params = Map("appid" -> appId, "q" -> ort, "units" -> "metric")
    val context = endpoint.withQuery(Query(params))
    HttpRequest(uri = context, method = HttpMethods.GET)
      .withEffectiveUri(securedConnection = endpoint.effectivePort == 443,
        defaultHostHeader = Host(endpoint.authority.host))
  }.log("buildRequest")

  val call = Flow[HttpRequest].mapAsync(parallelism=1) { request =>
    println(s"Request: $request")
    Http().singleRequest(request)
  }.log("call")

  val accept = Flow[HttpResponse].mapAsync(parallelism = 1) {
    case response if response.status == StatusCodes.NoContent =>
      println("(no content)")
      response.discardEntityBytes().future() map (_ => None)

    case response if response.status == StatusCodes.Unauthorized =>
      val message = response
      response.discardEntityBytes()
      throw new SecurityException(message.toString())

    case response if response.status.isSuccess =>
      response.entity.toStrict(1 second) map { value =>
        val message = value.data.utf8String
        if(message.nonEmpty) {
          println(s"Response: $message")
          Some(message)
        } else {
          None
        }
      }
    case response =>
      println(s"(non-success: $response)")
      response.discardEntityBytes().future() map (_ => None)

  }.collect { case Some(t) => t }

  val parser = Flow[String].map { response =>
    Json.parse(response).validate[Response]
  }.log("parser")

  val errorSink = Sink.foreach[JsResult[Response]] {
    case response: JsError =>
      response.errors foreach { error =>
        println(s"Error: ${error._2.head} @ ${error._1}")
      }
    case _ =>
  }

  val diversion = Flow[JsResult[Response]].divertTo(errorSink, _.isError)

  val extractor = Flow[JsResult[Response]].collect { case response: JsSuccess[Response] =>
    response.value
  }

  val process = source via buildRequest via call via accept via parser via diversion via extractor runWith Sink.seq

  process onComplete {
    case Success(result) =>
      result foreach println
      system.terminate
    case Failure(t) =>
      println(s"Failure: ${t.getMessage}")
      system.terminate
  }
}
