package jetstream.app

import java.util.Properties
import akka.http.scaladsl.model.Uri
import scala.io.Source

case class Config(properties: Properties) {
  require(properties.containsKey(Config.WeatherURL),
    s"properties must contain value for '${Config.WeatherURL}'")
  require(properties.containsKey(Config.WeatherAppId),
    s"properties must contain value for '${Config.WeatherAppId}'")
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
      val props = new Properties()
      props.load(Source.fromURL(url).bufferedReader())
      props
    }
    Config(props.get)
  }
  lazy val default = Config("/endpoints.properties")

  private def parseOptions() = new scopt.OptionParser[Config]("jetstream") {
    val update = (props: Properties, kv: (String,String)) => {
      props.replace(kv._1, kv._2)
      props
    }
    head("jetstream", "0.1")
    opt[String]('w', "wurl").valueName("<weather url>").action((w, opts) =>
      opts.copy(properties=update(opts.properties,(Config.WeatherURL, w))))
      .text("url of weather service")
    opt[String]('k', "apiKey").valueName("<api key>").action((k, opts) =>
      opts.copy(properties=update(opts.properties,(Config.WeatherAppId, k))))
      .text("api key for weather service")
    help("help").text("look-up current weather")
    note("""Jetstream -- Looks-up current weather reports
           |---------------------------------------------
           |An interactive app to query the current weather
           |conditions at a specified location (town, country)
         """.stripMargin)
  }

  def commandLine(args: Array[String]) =
    Config.parseOptions()
      .parse(args, default)
      .getOrElse(default)

}