package jetstream.app

import java.util.Properties
import akka.http.scaladsl.model.Uri
import scala.io.Source

case class Config(properties: Properties) {
  require(properties.containsKey(Config.WeatherURLKey),
    s"properties must contain value for '${Config.WeatherURLKey}'")
  require(Uri(properties.getProperty(Config.WeatherURLKey)).isAbsolute,
    s"property ${Config.WeatherURLKey} must be a valid URL")
  require(properties.containsKey(Config.WeatherAppIdKey),
    s"properties must contain value for '${Config.WeatherAppIdKey}'")
  require(properties.getProperty(Config.WeatherAppIdKey).nonEmpty,
    s"property ${Config.WeatherAppIdKey} must have a value")

  val weatherUrl = Uri(properties.getProperty(Config.WeatherURLKey))
  val weatherApiId = properties.getProperty(Config.WeatherAppIdKey)
  val apiDispatcher = properties.getProperty(Config.ApiDispatcherKey,"akka.actor.api-dispatcher")
  val streamWidth = properties.getProperty(Config.StreamWidth,"1").toInt

}

object Config {
  val WeatherURLKey = "weather-url"
  val WeatherAppIdKey = "weather-app-id"
  val ApiDispatcherKey = "api-dispatcher"
  val StreamWidth = "stream-width"

  def apply(propMap: Map[String,String]): Config = {
    val props = propMap.foldLeft(new Properties) { case (acc,(key,value)) =>
      acc.setProperty(key,value)
      acc
    }
    Config(props)
  }

  def apply(propertyFile: String): Config = {
    val maybeUrl = Option(getClass.getResource(propertyFile))
    require(maybeUrl.isDefined, s"property file '$propertyFile' must exists and be readable")
    val props = maybeUrl map { url =>
      val props = new Properties()
      props.load(Source.fromURL(url).bufferedReader())
      props
    }
    Config(props.get)
  }

  def apply(args: Array[String]): Config =
    Config.parseOptions()
      .parse(args, default)
      .getOrElse(default)

  lazy val default = Config("/endpoints.properties")

  private def parseOptions() = new scopt.OptionParser[Config]("jetstream") {
    val update = (props: Properties, key: String, value: String) => {
      props.replace(key, value)
      props
    }
    head("jetstream", "0.1")
    opt[String]('w', "wurl").valueName("<weather url>").action((w, opts) =>
      opts.copy(properties=update(opts.properties, Config.WeatherURLKey, w)))
      .text("url of weather service")
    opt[String]('k', "apiKey").valueName("<api key>").action((k, opts) =>
      opts.copy(properties=update(opts.properties, Config.WeatherAppIdKey, k)))
      .text("api key for weather service")
    help("help").text("look-up current weather")
    note("""Jetstream -- Looks-up current weather reports
           |---------------------------------------------
           |An interactive app to query the current weather
           |conditions at a specified location (town, country)
         """.stripMargin)
  }

}