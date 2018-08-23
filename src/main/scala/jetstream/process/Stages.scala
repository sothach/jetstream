package jetstream.process

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.headers.Host
import akka.stream.{ActorAttributes, ActorMaterializer}
import akka.stream.scaladsl.{Flow, Sink}
import argonaut.Parse
import com.typesafe.scalalogging.Logger
import jetstream.app.Config
import jetstream.model.weather.Report

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class Stages(config: Config)(
  private implicit val system: ActorSystem,
  private implicit val materializer: ActorMaterializer,
  private implicit val ec: ExecutionContext) {

  import jetstream.conversions.WeatherJsonProtocol._
  import config._
  val logger = Logger(this.getClass)

  val buildRequest = Flow[(String,String)].map { case (town,country) =>
    val params = Map("appid" -> weatherApiId, "q" -> s"$town,$country", "units" -> "metric")
    val context = weatherUrl.withQuery(Query(params))
    HttpRequest(uri = context, method = HttpMethods.GET)
      .withEffectiveUri(securedConnection = weatherUrl.effectivePort == 443,
        defaultHostHeader = Host(weatherUrl.authority.host))
  }

  val call = Flow[HttpRequest].mapAsync(parallelism=streamWidth) { request =>
    Http().singleRequest(request)
  }.withAttributes(ActorAttributes.dispatcher(apiDispatcher))

  val accept = Flow[HttpResponse].mapAsync(parallelism = streamWidth) {
    case response if response.status == StatusCodes.NoContent =>
      response.discardEntityBytes().future() map (_ => Left("No content"))

    case response if response.status == StatusCodes.Unauthorized =>
      val message = response
      response.discardEntityBytes()
      throw new SecurityException(message.toString())

    case response if response.status.isSuccess =>
      response.entity.toStrict(1 second) map { value =>
        Right(value.data.utf8String)
      }
    case response =>
      response.discardEntityBytes().future() map (_ => Left("Not found"))
  }

  val parser = Flow[Either[String,String]].map {
    case Right(report) =>
      Parse.decodeEither[Report](report): Either[String, Report]
    case Left(error) =>
      Left(error): Either[String, Report]
  }

}
