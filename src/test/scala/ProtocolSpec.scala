import argonaut.Argonaut._
import jetstream.model._
import jetstream.model.weather._
import org.scalatest.{FlatSpec, Matchers}

class ProtocolSpec extends FlatSpec with Matchers {

  import jetstream.conversions.WeatherJsonProtocol._

  "A Response" should "be serialized to Json in" in {
    val subject = Report("", None, 1,
      1, "", 1,
      Coordinate(-77.0365, 38.8977), List(Weather(1, "", "", "")),
      Main(19.9, 6, 22, 1024, 97),
      Wind(64.4, Some(180)), Clouds(23),
      Sys(Some(1), Some(1), 1.0, "io", 1234566, 2737373))
    val expectJson = """{"base":"","visibility":null,"dt":1,"id":1,"name":"","cod":1,"coord":{"lon":-77.0365,"lat":38.8977},
                        |"weather":[{"id":1,"main":"","description":"","icon":""}],
                        |"main":{"temp":19.9,"temp_min":6,"temp_max":22,"pressure":97},
                        |"wind":{"speed":64.4,"deg":180},"clouds":{"all":23},
                        |"sys":{"type":1,"id":1,"message":1.0,"country":"io","sunrise":1234566,"sunset":2737373}}""".stripMargin.replaceAll("\n","")

    subject.asJson.toString shouldBe expectJson
  }

}
