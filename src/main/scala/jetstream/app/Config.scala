package jetstream.app

import java.util.Properties

import akka.http.scaladsl.model.Uri

import scala.io.Source

case class Config(properties: Properties) {
  require(properties.containsKey("weather-api"), s"property map must contain value for 'weather-api'")
  require(properties.containsKey("weather-app-id"), "property map must contain value for 'weather-app-id'")
  val weatherUrl = Uri(properties.getProperty("weather-api"))
  val weatherApiId = properties.getProperty("weather-app-id")
}

object Config {
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
}