import java.io.{ByteArrayInputStream, ByteArrayOutputStream, PrintStream}

import jetstream.app.{ConsoleInOut, Jetstream, JetstreamRepl}
import org.scalatest.{FlatSpec, Matchers}

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

    val commands = "?\nMaynooth,ie\nq\n".getBytes
    val expected = "Current Weather in Maynooth"
    System.setIn(new ByteArrayInputStream(commands))
    val out = new ByteArrayOutputStream
    System.setOut(new PrintStream(out))

    Jetstream.main(Array.empty)

    //out.toString should contain(expected)
  }

}
