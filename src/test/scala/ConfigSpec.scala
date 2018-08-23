import akka.http.scaladsl.model.Uri
import jetstream.app.Config
import org.scalatest.{FlatSpec, Matchers}

class ConfigSpec extends FlatSpec with Matchers {

  "The configuration" should "be created in" in {
    val subject = Config("/endpoints.properties")
    subject.weatherApiId shouldBe "12345"
    subject.weatherUrl shouldBe Uri("http://localhost/weather")
  }

  "The configuration" should "fail requirement to contain property for 'weather-url'" in {
    the[IllegalArgumentException] thrownBy {
      Config("/bad.properties")
    } should have message "requirement failed: properties must contain value for 'weather-url'"
  }

  "The configuration" should "fail requirement for a valid URL for 'weather-url'" in {
    the[IllegalArgumentException] thrownBy {
      Config(Map(Config.WeatherURLKey -> "/localhost/weather"))
    } should have message "requirement failed: property weather-url must be a valid URL"
  }

  "The configuration" should "fail requirement to contain property 'weather-app-id'" in {
    the[IllegalArgumentException] thrownBy {
      Config(Map(Config.WeatherURLKey -> "http://localhost/weather"))
    } should have message "requirement failed: properties must contain value for 'weather-app-id'"
  }

  "The configuration" should "fail requirement to contain value for 'weather-app-id'" in {
    the[IllegalArgumentException] thrownBy {
      Config(Map(Config.WeatherURLKey -> "http://localhost/weather", Config.WeatherAppIdKey -> ""))
    } should have message "requirement failed: property weather-app-id must have a value"
  }

  "The configuration" should "fail requirement that the file should exist" in {
    the[IllegalArgumentException] thrownBy {
      Config("/absent.properties")
    } should have message "requirement failed: property file '/absent.properties' must exists and be readable"
  }

}
