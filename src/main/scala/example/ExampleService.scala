package example

import org.http4s._
import org.http4s.dsl._
import org.http4s.server._

import scala.concurrent.ExecutionContext

object ExampleService {

  // A Router can mount multiple services to prefixes.  The request is passed to the
  // service with the longest matching prefix.
  def service(implicit executionContext: ExecutionContext = ExecutionContext.global): HttpService = Router(
    "" -> rootService
  )

  def rootService(implicit executionContext: ExecutionContext) = HttpService {
    case req @ GET -> Root =>
      Ok("index")

    case _ -> Root =>
      // The default route result is NotFound. Sometimes MethodNotAllowed is more appropriate.
      MethodNotAllowed()

    case GET -> Root / "ping" =>
      // EntityEncoder allows for easy conversion of types to a response body
      Ok("pong")

    case req @ POST -> Root / "echo" =>
      Ok(req.bodyAsText)
  }
}