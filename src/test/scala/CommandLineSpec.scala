import java.io.{ByteArrayInputStream, ByteArrayOutputStream, PrintStream}

import jetstream.app.{Config, ConsoleInOut, Jetstream, JetstreamRepl}
import net.jadler.Jadler.port
import org.scalatest.{FlatSpec, Matchers}
import net.jadler.Jadler._

class CommandLineSpec extends FlatSpec with Matchers {

  "The command line interpreter" should "execute commands" in {
    val commands = "?\nq\n"
    val expected =
      """Please enter town,country: q:	exit
        |?:	display this help
        |town,country:	name town and country (ISO3166 two-letter code)
        |Please enter town,country: """.stripMargin
    val in = new ByteArrayInputStream(commands.getBytes())
    val out = new ByteArrayOutputStream
    val io = new ConsoleInOut(in,new PrintStream(out))
    new JetstreamRepl(io,Config.default).run()
    out.toString shouldBe expected
  }

  "The command line interpreter" should "highlight invalid commands commands" in {
    val commands = "x\nq\n"
    val expected =
      """Please enter town,country: input error: [x]: enter q or town,country
        |Please enter town,country: """.stripMargin
    val in = new ByteArrayInputStream(commands.getBytes())
    val out = new ByteArrayOutputStream
    val io = new ConsoleInOut(in,new PrintStream(out))
    new JetstreamRepl(io,Config.default).run()

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
        |"id":2964574,"name":"Maynooth","cod":200}""".stripMargin
    initJadler()
    onRequest()
      .havingMethodEqualTo("GET")
      .havingPathEqualTo("/data/2.5/weather")
      .respond().withBody(expectedJson).withStatus(200)

    val commands = "Maynooth,ie\nq\n".getBytes
    val expected = """Please enter town,country: Current Weather in Maynooth: light intensity drizzle 18.0c wind: 5.7 kph W IE daylight: 05:12:31 to 19:43:23
                    |Please enter town,country: """.stripMargin
    System.setIn(new ByteArrayInputStream(commands))
    val out = new ByteArrayOutputStream
    System.setOut(new PrintStream(out))

    Jetstream.main(Array(s"-w=http://localhost:${port()}/data/2.5/weather"))
    closeJadler()
    out.toString shouldBe expected
  }

  "When an invalid country code is provided, the Repl" should "report the error" in {
    import java.io.{ByteArrayInputStream, ByteArrayOutputStream, PrintStream}

    val commands = "Maynooth,XX\nq\n".getBytes
    val expected = """Please enter town,country: input error: [maynooth,xx]: enter q or town,country
                     |Please enter town,country: """.stripMargin
    System.setIn(new ByteArrayInputStream(commands))
    val out = new ByteArrayOutputStream
    System.setOut(new PrintStream(out))

    Jetstream.main(Array(s"-w=http://localhost/data/2.5/weather"))

    out.toString shouldBe expected
  }

  "When an invalid town is provided, the Repl" should "report the error" in {
    import java.io.{ByteArrayInputStream, ByteArrayOutputStream, PrintStream}
    initJadler()
    onRequest()
      .havingMethodEqualTo("GET")
      .havingPathEqualTo("/data/2.5/weather")
      .respond().withStatus(404)

    val commands = "Killinaskully,IE\nq\n".getBytes
    val expected = """Please enter town,country: Not found: 'killinaskully'
                     |Please enter town,country: """.stripMargin
    System.setIn(new ByteArrayInputStream(commands))
    val out = new ByteArrayOutputStream
    System.setOut(new PrintStream(out))

    Jetstream.main(Array(s"-w=http://localhost:${port()}/data/2.5/weather"))
    closeJadler()

    out.toString shouldBe expected
  }

}
