import argonaut.Argonaut._
import jetstream.model._
import jetstream.model.weather._
import org.scalatest.{FlatSpec, Matchers}

class ProtocolSpec extends FlatSpec with Matchers {

  import jetstream.conversions.WeatherJsonProtocol._

  "A Response" should "be serialized to Json in" in {
    val subject = Response("", None, 1,
      1, "", 1,
      Coordinate(-77.0365, 38.8977), List(Weather(1, "", "", "")),
      Main(19.9, 6, 22, 1024, 97),
      Wind(64.4, Some(180)), Clouds(23),
      Sys(Some(1), Some(1), 1.0, "io", 1234566, 2737373))
    subject.asJson
  }

}
