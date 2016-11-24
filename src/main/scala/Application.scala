import com.codahale.metrics._
import example.{ExampleService, Zauth}
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.metrics._
import org.http4s.server.{Router, ServerApp}

object Application extends ServerApp {
  val metricRegistry = new MetricRegistry()

  val srvc = Router(
    "" -> Metrics(metricRegistry)(Zauth(ExampleService.service)),
    "/metrics" -> metricsService(metricRegistry)
  )

  def server(args: List[String]) = BlazeBuilder.bindHttp(8080)
    .mountService(srvc, "/http4s")
    .start
}
