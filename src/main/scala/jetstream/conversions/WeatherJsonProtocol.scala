package jetstream.conversions

import jetstream.model.weather._
import argonaut._
import Argonaut._
import jetstream.model.Coordinate

object WeatherJsonProtocol {
    implicit def formatCoordinate =
      casecodec2(Coordinate.apply, Coordinate.unapply)("lon", "lat")
    implicit def formatMain =
      casecodec5(Main.apply, Main.unapply)("temp", "temp_min", "temp_max", "pressure", "pressure")
    implicit def formatWeather =
      casecodec4(Weather.apply, Weather.unapply)("id", "main", "description", "icon")
    implicit def formatWind =
      casecodec2(Wind.apply, Wind.unapply)("speed", "deg")
    implicit def formatClouds =
      casecodec1(Clouds.apply, Clouds.unapply)("all")
    implicit def formatSys =
      casecodec6(Sys.apply, Sys.unapply)("type", "id", "message", "country", "sunrise", "sunset")
    implicit def formatResponse =
      casecodec12(WeatherReport.apply, WeatherReport.unapply)(
        "base", "visibility", "dt", "id",
        "name", "cod", "coord", "weather",
        "main", "wind", "clouds", "sys")
  }
