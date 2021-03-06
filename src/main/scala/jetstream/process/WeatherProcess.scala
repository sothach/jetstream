package jetstream.process

import akka.actor.ActorSystem
import akka.stream.Supervision.{Decider, Resume, Stop}
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import com.typesafe.scalalogging.Logger
import jetstream.app.Config

import scala.collection.immutable

class WeatherProcess(config: Config)(implicit val system: ActorSystem) {
  val logger = Logger(this.getClass)
  val decider: Decider = {
    case ex: SecurityException =>
      logger.warn(s"WeatherProcess stopping because of ${ex.getClass.getCanonicalName} (${ex.getMessage})")
      Stop
    case ex =>
      logger.warn(s"WeatherProcess resuming because of ${ex.getClass.getCanonicalName} (${ex.getMessage})")
      logger.warn(s"WeatherProcess caught", ex)
      Resume
  }
  implicit val materializer = ActorMaterializer(
    ActorMaterializerSettings(system)
      .withSupervisionStrategy(decider))(system)
  implicit val ec = system.dispatcher

  val stages = new Stages(config)
  import stages._

  val process =  weatherRequest via call via accept via weatherParser

  def lookup(town: String, country: String) =
    Source.single((town,country)) via process runWith Sink.seq

}
