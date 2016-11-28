import com.codahale.metrics._
import example.{ExampleService, Zauth}
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.metrics._
import org.http4s.server.{Router, ServerApp}
import org.log4s.getLogger
import appdyn.metrics._

import scalaz.{-\/, \/-}

object Application extends ServerApp {
  val metricRegistry = new MetricRegistry()
  val logger = getLogger

  val srvc = Router(
    "" -> Metrics(metricRegistry)(Zauth(ExampleService.service)),
    "/metrics" -> metricsService(metricRegistry),
    "/health"  -> ExampleService.health
  )

  def server(args: List[String]) = {
    ReportMetrics.publish(metricRegistry, ReportMetrics.toAppdynMetric, ReportMetrics.reset(metricRegistry))
      .run.runAsync {
        case -\/(e) => logger.error(e)("unexpected error while publishing metrics")
        case \/-(_) => ()
      }
    BlazeBuilder.bindHttp(8080, "0.0.0.0")
      .mountService(srvc, "/http4s")
      .start
  }
}
