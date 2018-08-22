package jetstream.model.weather

import java.time.{LocalDateTime, ZoneOffset}

import jetstream.model.Coordinate

case class Weather(id: Int, main: String, description: String, icon: String)
case class Main(temp: Double, tempMin: Int, tempMax: Int, pressure: Int, humidity: Int)

case class Wind(speed: Double, deg: Option[Int]) {
  override def toString = {
    def degToCompass(num: Option[Int]) = num match {
      case Some(n) =>
        val compassPoints = Array("N", "NNE", "NE", "ENE", "E", "ESE", "SE",
          "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW")
        val v = ((n / 22.5) + 0.5).toInt
        compassPoints(v % 16)
      case None =>
        "?"
    }
    s"$speed kph ${degToCompass(deg)}"
  }
}
case class Clouds(all: Int)
case class Sys(`type`: Option[Int], id: Option[Int], message: Double, country: String, sunrise: Long, sunset: Long) {
  override def toString = {
    val sunup = LocalDateTime.ofEpochSecond(sunrise,0,ZoneOffset.UTC).toLocalTime
    val sundown = LocalDateTime.ofEpochSecond(sunset,0,ZoneOffset.UTC).toLocalTime
    s"$country daylight: $sunup to $sundown"
  }
}

case class Response(base: String, visibility: Option[Int], dt: Long,
                    id: Long, name: String, cod: Int,
                    coord: Coordinate, weather: List[Weather], main: Main,
                    wind: Wind, clouds: Clouds, sys: Sys) {
  override def toString = {
    val w = weather.head
    s"Current Weather in $name: ${w.description} ${main.temp}c wind: $wind $sys"
  }
}
