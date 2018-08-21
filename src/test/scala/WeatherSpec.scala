import java.util.concurrent.Executors

import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.concurrent.Waiters._
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class WeatherSpec extends FlatSpec with Matchers with ScalaFutures with MockitoSugar with BeforeAndAfterAll {
  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))

  "when message is posted to the queue, the client" should "deliver the message" in {

    val waiter = new Waiter
    waiter.dismiss
    waiter.await(timeout(2 seconds), dismissals(1))
  }
}
