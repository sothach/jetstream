package jetstream.app

import akka.actor.ActorSystem

import scala.concurrent.Await
import scala.concurrent.duration._

trait State {
  def action(): State
}

object Finished extends State {
  def action() = this
}

trait InputState extends State {
  val exit = "q"
  val help = "?"
  def console: ConsoleInOut
  def commandHelp: Seq[(String,String)] = Seq(
    ("q", "exit"),
    ("?", "display this help")
  )
  def prompt: String
  def next(input: String): State
  def action() = {
    console.print(prompt)
    console.readLine().trim.toLowerCase match {
      case v if v == exit =>
        Finished
      case v if v == help =>
        DisplayHelp(this)
      case v =>
        next(v)
    }
  }
  def onError(input: String) = console.print(s"input error: $input\n")
}
case class DisplayHelp(parent: InputState) extends State {
  def action() = {
    parent.commandHelp foreach { case (command, purpose) =>
      parent.console.print(s"$command:\t$purpose\n")
    }
    parent.action()
  }
}

case class LookupWeather(console: ConsoleInOut, config: Config)(implicit system: ActorSystem) extends InputState {
  val prompt = s"Please enter town/country: "
  val placePattern = """\s*(\w+)\s*,\s*(\w+)\s*""".r
  val weatherProcess = new WeatherProcess(config)
  override val commandHelp = super.commandHelp :+
    ("town,country" -> "name town and country (two-letter code)")
  def next(value: String): State = {
    value match {
      case placePattern(town,country) =>
        console.print(s"town=$town country=$country\n")
        val result = Await.result(weatherProcess.lookup(town,country), 10 seconds)
        result foreach (r => console.print(s"$r\n"))
        this
      case error =>
        onError(s"[$error]: enter $exit or town/country")
        this
    }
  }
}

class JetstreamRepl(console: ConsoleInOut, config: Config) {
  import scala.annotation.tailrec
  implicit val system = ActorSystem("weather-system")

  @tailrec
  private def inputLoop(next: State): State = next match {
    case Finished =>
      system.terminate()
      Finished
    case state => inputLoop(state.action())
  }

  def run() = inputLoop(LookupWeather(console,config))
}

object Jetstream {
  def main(args: Array[String]): Unit = {
    new JetstreamRepl(new ConsoleInOut(),
      Options.commandLine()
        .parse(args, Config.default)
        .getOrElse(Config.default)
    ).run()
  }
}