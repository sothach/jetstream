package jetstream.app

import java.util.Properties

import akka.http.scaladsl.model.Uri

import scala.io.Source

case class Config(properties: Properties) {
  require(properties.containsKey(Config.WeatherURL), s"property map must contain value for '${Config.WeatherURL}'")
  require(properties.containsKey(Config.WeatherAppId), s"property map must contain value for '${Config.WeatherAppId}'")
  val weatherUrl = Uri(properties.getProperty(Config.WeatherURL))
  val weatherApiId = properties.getProperty(Config.WeatherAppId)
}

object Config {
  val WeatherURL = "weather-url"
  val WeatherAppId = "weather-app-id"
  def apply(propMap: Map[String,String]): Config = {
    val props = propMap.foldLeft(new Properties) { case (acc,item) =>
      acc.setProperty(item._1,item._2)
      acc
    }
    Config(props)
  }
  def apply(propertyFile: String): Config = {
    val maybeUrl = Option(getClass.getResource(propertyFile))
    require(maybeUrl.isDefined, s"property file '$propertyFile' must exists and be readable")
    val props = maybeUrl map { url =>
      val source = Source.fromURL(url)
      val props = new Properties()
      props.load(source.bufferedReader())
      props
    }
    Config(props.get)
  }
  val default = Config("/endpoints.properties")
}