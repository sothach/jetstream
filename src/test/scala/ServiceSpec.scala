import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import jetstream.app.Config
import jetstream.model.weather.Report
import jetstream.process.WeatherProcess
import net.jadler.Jadler._
import net.jadler.Jadler.port
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.concurrent.Waiters._
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.collection.immutable
import scala.concurrent.Future
import scala.concurrent.duration._

class ServiceSpec extends FlatSpec with Matchers  with ScalaFutures with BeforeAndAfterAll {
  implicit val system = ActorSystem.create("TestActorSystem")
  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system))
  implicit val ec = system.dispatcher

  "Calling the streaming service " should "return a sequence of results" in {
    val waiter = new Waiter
    val config = Config(Map(Config.WeatherURLKey -> s"http://localhost:$port/data/2.5/weather", Config.WeatherAppIdKey -> ""))
    val weatherProcess = new WeatherProcess(config)
    resetJadler()
    onRequest()
      .havingMethodEqualTo("GET")
      .havingPathEqualTo("/data/2.5/weather")
      .respond().withBody(nextResponse()).withStatus(200)

    val testProcess: Future[Seq[Report]] = Source(testLocations) via weatherProcess.process runWith Sink.seq

    whenReady(testProcess, Timeout(10 seconds)) { responses =>
      responses.length shouldBe 4
      waiter.dismiss
    }
    waiter.await(timeout(10 seconds), dismissals(1))
  }

  val testLocations = immutable.Seq(
    ("Dublin","ie"),
    ("Munich","de"),
    ("Oxford","uk"),
    ("Durban","sa")
  )
  var nextItem = 0
  private def nextResponse(): String = {
    val idx = nextItem
    nextItem = nextItem + 1
   s""" {"coord":{"lon":-6.$idx,"lat":53.35},
        "weather":
        [{"id":$idx,"main":"Drizzle","description":"light intensity drizzle","icon":"09d"}],
        "base":"stations","main":{"temp":18,"pressure":1016,"humidity":88,"temp_min":18,"temp_max":18},
        "visibility":10000,"wind":{"speed":5.7},
        "clouds":{"all":75},"dt":1534676400,
        "sys":{"type":1,"id":5237,"message":0.0052,"country":"${testLocations(idx)._2.toUpperCase}",
        "sunrise":1534655551,"sunset":1534707803},
        "id":2964574,"name":"${testLocations(idx)._1}","cod":200}
      """
  }

  override def beforeAll = {
    initJadler()
  }

  override def afterAll {
    closeJadler()
  }
}
