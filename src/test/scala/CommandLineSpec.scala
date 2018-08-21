import java.io.{ByteArrayInputStream, ByteArrayOutputStream, PrintStream}

import jetstream.app.{ConsoleInOut, Jetstream, JetstreamRepl}
import net.jadler.Jadler.port
import org.scalatest.{FlatSpec, Matchers}
import net.jadler.Jadler._

class CommandLineSpec extends FlatSpec with Matchers {

  "The command line interpreter" should "execute commands" in {
    val commands = "?\nq\n"
    val expected =
      """Please enter town/country: q:	exit
        |?:	display this help
        |town,country:	name town and country (two-letter code)
        |Please enter town/country: """.stripMargin
    val in = new ByteArrayInputStream(commands.getBytes())
    val out = new ByteArrayOutputStream
    val io = new ConsoleInOut(in,new PrintStream(out))
    new JetstreamRepl(io).run()
    out.toString shouldBe expected
  }

  "The command line interpreter" should "highlight invalid commands commands" in {
    val commands = "x\nq\n"
    val expected =
      """Please enter town/country: input error: [x]: enter q or town/country
        |Please enter town/country: """.stripMargin
    val in = new ByteArrayInputStream(commands.getBytes())
    val out = new ByteArrayOutputStream
    val io = new ConsoleInOut(in,new PrintStream(out))
    new JetstreamRepl(io).run()

    out.toString shouldBe expected
  }

  "When the Repl is launched, the Std IO" should "be connected" in {
    import java.io.{ByteArrayInputStream, ByteArrayOutputStream, PrintStream}
    val expectedJson =
      """{"coord":{"lon":-6.26,"lat":53.35},
        |"weather":[{"id":300,"main":"Drizzle","description":"light intensity drizzle","icon":"09d"}],
        |"base":"stations","main":{"temp":18,"pressure":1016,"humidity":88,"temp_min":18,"temp_max":18},
        |"visibility":10000,"wind":{"speed":5.7,"deg":280},"clouds":{"all":75},"dt":1534676400,
        |"sys":{"type":1,"id":5237,"message":0.0052,"country":"IE","sunrise":1534655551,"sunset":1534707803},
        |"id":2964574,"name":"Dublin","cod":200}""".stripMargin
    initJadler()
    onRequest()
      .havingMethodEqualTo("GET")
      .havingPathEqualTo("/data/2.5/weather")
      .respond().withBody(expectedJson).withStatus(200)

    val commands = "Maynooth,ie\nq\n".getBytes
    val expected = """Please enter town/country: town=maynooth country=ie
                     |Current Weather in Dublin: light intensity drizzle 18.0c wind: 5.7 kph W IE daylight: 05:12:31 to 19:43:23
                     |Please enter town/country: """.stripMargin
    System.setIn(new ByteArrayInputStream(commands))
    val out = new ByteArrayOutputStream
    System.setOut(new PrintStream(out))
    def defaultConfig = Map(
      "weather-api" -> s"http://localhost:${port()}/data/2.5/weather",
      "weather-app-id" -> "12345")

    Jetstream.main(Array(s"http://localhost:${port()}/data/2.5/weather"))
    closeJadler()
println(s">$out<")
    out.toString shouldBe expected
  }

}
