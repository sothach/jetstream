import java.net.ConnectException

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpRequest
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import jetstream.app.{Config, WeatherProcess}
import jetstream.process.Stages
import net.jadler.Jadler._
import net.jadler.Request
import net.jadler.stubbing.StubResponse
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.concurrent.Waiters._
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.concurrent.duration._
import scala.util.Failure

class WeatherApiSpec extends FlatSpec with Matchers with ScalaFutures with BeforeAndAfterAll {
  implicit val system = ActorSystem.create("TestActorSystem")
  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system))
  implicit val ec = system.dispatcher

  def defaultConfig = Map(
    Config.WeatherURL -> s"http://localhost:${port()}/data/2.5/weather",
    Config.WeatherAppId -> "12345")

  "when the weather API call fails, the error" should "be handled correctly" in {
    val waiter = new Waiter
    val config = Config(Map(Config.WeatherURL -> s"http://localhost:$port/data/2.5/weather", Config.WeatherAppId -> ""))

    val weather = new WeatherProcess(config) {
      val badCall = Flow[HttpRequest].mapAsync(parallelism=1) { _ =>
        throw new RuntimeException("call failed")
      }
      val badProcess = Source.single(("Dublin","ie")) via stages.buildRequest via badCall runWith Sink.headOption
    }

    whenReady(weather.badProcess, Timeout(10 seconds)) {
      case Seq(_) =>
        fail
      case _ =>
        waiter.dismiss
    }
    waiter.await(timeout(10 seconds), dismissals(1))
  }

  "full process spec" should "work" in {
    val waiter = new Waiter
    val config = Map(Config.WeatherURL -> s"http://localhost:$port/data/2.5/weather", Config.WeatherAppId -> "")
    val stages = new Stages(Config(config))
    import stages._
    resetJadler()
    val expectedJson ="""
        {"coord":{"lon":-6.26,"lat":53.35},
        "weather":
        [{"id":300,"main":"Drizzle","description":"light intensity drizzle","icon":"09d"}],
        "base":"stations","main":{"temp":18,"pressure":1016,"humidity":88,"temp_min":18,"temp_max":18},
        "visibility":10000,"wind":{"speed":5.7},
        "clouds":{"all":75},"dt":1534676400,
        "sys":{"type":1,"id":5237,"message":0.0052,"country":"IE","sunrise":1534655551,"sunset":1534707803},
        "id":2964574,"name":"Dublin","cod":200}
      """.stripMargin
    onRequest()
      .havingMethodEqualTo("GET")
      .havingPathEqualTo("/data/2.5/weather")
      .respond().withBody(expectedJson).withStatus(200)
      .thenRespond().withStatus(404)

    val process = Source.single(("Dublin","ie")) via buildRequest via call via accept via parser via extractor runWith Sink.seq

    whenReady(process, Timeout(10 seconds)) {
      case Seq(response) =>
        response.wind.toString shouldBe "5.7 kph ?"
        waiter.dismiss
      case Nil =>
        fail
    }
    waiter.await(timeout(10 seconds), dismissals(1))
  }

  "Unauthorized error in the service" should "be caught" in {
    val waiter = new Waiter
    resetJadler()
    onRequest()
      .havingMethodEqualTo("GET")
      .havingPathEqualTo("/data/2.5/weather")
      .respond().withStatus(401)

    val weather = new WeatherProcess(Config(defaultConfig))
    weather.lookup("Dublin","ie") onComplete {
      case Failure(_: SecurityException) =>
        waiter.dismiss
      case _ =>
       fail
    }
    waiter.await(timeout(10 seconds), dismissals(1))
  }

  "No content responses in the service" should "be fine" in {
    val waiter = new Waiter
    resetJadler()
    onRequest()
      .havingMethodEqualTo("GET")
      .havingPathEqualTo("/data/2.5/weather")
      .respond().withStatus(204)

    val weather = new WeatherProcess(Config(defaultConfig))
    val process = weather.lookup("Dublin","ie")
    whenReady(process, Timeout(10 seconds)) {
      case Seq(_) =>
        fail
      case Nil =>
        waiter.dismiss
    }
    waiter.await(timeout(10 seconds), dismissals(1))
  }

  "4xx errors in the service" should "be caught" in {
    val waiter = new Waiter
    resetJadler()
    onRequest()
      .havingMethodEqualTo("GET")
      .havingPathEqualTo("/data/2.5/weather")
      .respond().withStatus(404)

    val weather = new WeatherProcess(Config(defaultConfig))
    val process = weather.lookup("Dublin","ie")
    whenReady(process, Timeout(10 seconds)) {
      case Seq(_) =>
        fail
      case Nil =>
        waiter.dismiss
    }
    waiter.await(timeout(10 seconds), dismissals(1))
  }

  "An invalid JSON response" should "be caught" in {
    val waiter = new Waiter
    resetJadler()
    onRequest()
      .havingMethodEqualTo("GET")
      .havingPathEqualTo("/data/2.5/weather")
      .respond().withBody("{xxx}").withStatus(200)

    val weather = new WeatherProcess(Config(defaultConfig))
    val process = weather.lookup("Dublin","ie")
    whenReady(process, Timeout(10 seconds)) {
      case Seq(_) =>
        fail
      case Nil =>
        waiter.dismiss
    }
    waiter.await(timeout(10 seconds), dismissals(1))
  }

  "An exception in the service" should "be caught" in {
    val waiter = new Waiter
    resetJadler()
    val responder = new net.jadler.stubbing.Responder {
      override def nextResponse(request: Request): StubResponse =
        throw new ConnectException
    }
    onRequest()
      .havingMethodEqualTo("GET")
      .havingPathEqualTo("/data/2.5/weather")
      .respondUsing(responder)

    val weather = new WeatherProcess(Config(defaultConfig))
    val process = weather.lookup("Dublin","ie")
    whenReady(process, Timeout(10 seconds)) {
      case Seq(_) =>
        fail
      case Nil =>
        waiter.dismiss
    }
    waiter.await(timeout(10 seconds), dismissals(1))
  }

  "A runtime exception in the service" should "be caught" in {
    val waiter = new Waiter
    resetJadler()
    val responder = new net.jadler.stubbing.Responder {
      override def nextResponse(request: Request) =
        StubResponse.builder().body(null).build()
    }
    onRequest()
      .havingMethodEqualTo("GET")
      .havingPathEqualTo("/data/2.5/weather")
      .respondUsing(responder)

    val weather = new WeatherProcess(Config(defaultConfig))
    val process = weather.lookup("Dublin","ie")
    whenReady(process, Timeout(10 seconds)) {
      case Seq(_) =>
        fail
      case Nil =>
        waiter.dismiss
    }
    waiter.await(timeout(10 seconds), dismissals(1))
  }

  override def beforeAll = {
    initJadler()
  }

  override def afterAll {
    closeJadler()
  }
}
