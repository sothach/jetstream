package jetstream.app

import java.io._

class ConsoleInOut(in: InputStream = System.in, out: PrintStream = System.out) {
  val stdin = new BufferedReader(new InputStreamReader(in))
  val stdout = new PrintWriter(out)
  def readLine(): String = stdin.readLine
  def print(line: String): Unit = {
    stdout.print(line)
    stdout.flush()
  }
}