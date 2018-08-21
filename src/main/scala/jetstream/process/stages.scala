package jetstream.process

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.headers.Host
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink}
import argonaut.Parse
import jetstream.app.Config
import jetstream.model.weather.Response
import wvlet.log.LogSupport

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class Stages(config: Config)(
  private implicit val system: ActorSystem,
  private implicit val materializer: ActorMaterializer,
  private implicit val ec: ExecutionContext) extends LogSupport {

  import jetstream.conversions.WeatherJsonProtocol._
  import config._

  val buildRequest = Flow[(String,String)].map { case (town,country) =>
    val params = Map("appid" -> weatherApiId, "q" -> s"$town,$country", "units" -> "metric")
    val context = weatherUrl.withQuery(Query(params))
    HttpRequest(uri = context, method = HttpMethods.GET)
      .withEffectiveUri(securedConnection = weatherUrl.effectivePort == 443,
        defaultHostHeader = Host(weatherUrl.authority.host))
  }.log("buildRequest")

  val call = Flow[HttpRequest].mapAsync(parallelism=1) { request =>
    debug(s"Request: $request")
    Http().singleRequest(request)
  }.log("call")

  val accept = Flow[HttpResponse].mapAsync(parallelism = 1) {
    case response if response.status == StatusCodes.NoContent =>
      warn("(no content)")
      response.discardEntityBytes().future() map (_ => "")

    case response if response.status == StatusCodes.Unauthorized =>
      val message = response
      response.discardEntityBytes()
      throw new SecurityException(message.toString())

    case response if response.status.isSuccess =>
      response.entity.toStrict(1 second) map { value =>
        value.data.utf8String
      }
    case response =>
      warn(s"(non-success: $response)")
      response.discardEntityBytes().future() map (_ => "")

  }.collect { case s if s.nonEmpty => s }

  val parser = Flow[String].map { response =>
    Parse.decodeEither[Response](response)
  }

  val errorSink = Sink.foreach[Either[String,Response]] { error =>
    warn(s"Error: ${error.left.get}")
  }

  val extractor = Flow[Either[String,Response]]
    .divertTo(errorSink, _.isLeft)
    .collect {
      case Right(response) =>
        response
    }
}
