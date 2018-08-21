import akka.http.scaladsl.model.Uri
import jetstream.app.Config
import org.scalatest.{FlatSpec, Matchers}

class ConfigSpec extends FlatSpec with Matchers {

  "The configuration" should "be created in" in {
    val subject = Config("/test.properties")
    subject.weatherApiId shouldBe "12345"
    subject.weatherUrl shouldBe Uri("http://localhost/weather")
  }

  "The configuration" should "fail requirement to contain value for 'weather-api'" in {
    the[IllegalArgumentException] thrownBy {
      Config("/bad.properties")
    } should have message "requirement failed: property map must contain value for 'weather-api'"
  }

  "The configuration" should "fail requirement to contain value for 'weather-app-id'" in {
    the[IllegalArgumentException] thrownBy {
      Config(Map("weather-api" -> "http://localhost/weather"))
    } should have message "requirement failed: property map must contain value for 'weather-app-id'"
  }

  "The configuration" should "fail requirement that the file should exist" in {
    the[IllegalArgumentException] thrownBy {
      Config("/absent.properties")
    } should have message "requirement failed: property file '/absent.properties' must exists and be readable"
  }

}