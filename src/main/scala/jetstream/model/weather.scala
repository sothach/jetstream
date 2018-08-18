package jetstream.model

import java.time.{LocalDateTime, ZoneOffset}

object weather {

  case class Coordinate(lon: Double, lat: Double)
  case class Weather(id: Int, main: String, description: String, icon: String)
  case class Main(temp: Double, tempMin: Int, tempMax: Int, pressure: Int, humidity: Int)
  case class Wind(speed: Double, deg: Int) {
    override def toString = {
      def degToCompass(num: Int) = {
        val compassPoints = Array("N", "NNE", "NE", "ENE", "E", "ESE", "SE",
          "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
        val v = ((num / 22.5) + 0.5).toInt
        compassPoints(v % 16)
      }
      s"$speed kph ${degToCompass(deg)}"
    }
  }
  case class Clouds(all: Int)
  case class Sys(`type`: Int, id: Int, message: Double, country: String, sunrise: Long, sunset: Long) {
    override def toString = {
      val sunup = LocalDateTime.ofEpochSecond(sunrise,0,ZoneOffset.UTC).toLocalTime
      val sundown = LocalDateTime.ofEpochSecond(sunset,0,ZoneOffset.UTC).toLocalTime
      s"$country daylight: $sunup to $sundown"
    }
  }

  case class Response(base: String, visibility: Int, dt: Long,
                      id: Long, name: String, cod: Int,
                      coord: Coordinate, weather: Seq[Weather], main: Main,
                      wind: Wind, clouds: Clouds, sys: Sys) {
    override def toString = {
      val w = weather.head
      s"Current Weather in $name: ${w.description} ${main.temp}c wind: $wind $sys"
    }
  }
}
