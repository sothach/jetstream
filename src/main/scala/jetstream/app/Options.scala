package jetstream.app

import java.util.Properties

object Options {
  private def update(props: Properties, kv: (String,String)) = {
    props.replace(kv._1, kv._2)
    props
  }

  def commandLine() = new scopt.OptionParser[Config]("jetstream") {
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
}
