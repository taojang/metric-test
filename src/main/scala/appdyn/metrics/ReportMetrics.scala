package appdyn.metrics

import appdyn.metrics.domain.{Metric => AppdynMetric, _}
import com.codahale.metrics._
import org.http4s._
import org.http4s.client.blaze.PooledHttp1Client
import org.http4s.circe._
import io.circe.syntax._
import org.log4s.getLogger

import cats.data.NonEmptyList

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scalaz.concurrent.{Strategy, Task}
import scalaz.stream._
import scalaz.syntax.all._

object ReportMetrics {

  val logger = getLogger

  // a bunch of arguments of the component
  /**
    * uri of metrics endpoint of machine agent
    * [[https://docs.appdynamics.com/display/PRO42/Standalone+Machine+Agent+HTTP+Listener appdyn doc]]
    * [[https://github.com/zalando-stups/taupage/blob/9bec1294c78024e423ff782d0798b2959ca80e64/runtime/etc/init/appdynamics.conf taupage appdyn script]]
    */
  val metricsEndpointUri: Uri = Uri.uri("http://localhost:8293/api/v1/metrics")
  val client = PooledHttp1Client()
  val strategy = Strategy.DefaultStrategy
  val scheduler = Strategy.DefaultTimeoutScheduler
  val prefix = NonEmptyList("Server", List("Component:1840"))


  // example of how to convert Metric to AppdynMetric
  val toAppdynMetric: (String, Metric) => AppdynMetric = (name, metric) => {
    val metricPath = MetricPath(prefix ++ List(name))
    metric match {
      case m: Timer => AppdynMetric(metricPath, Average, (m.getOneMinuteRate * 1000).toLong) // TODO: find a better way to scale the value
      case m: Counter => AppdynMetric(metricPath, Sum, m.getCount)
      case other => throw new Exception("unsupported metric")
    }
  }

  // example of the "clean up" task after publish to appdyn
  def reset(mr: MetricRegistry): Task[Unit] = Task delay {
    mr.getMetrics.asScala.values.toList.foreach {
      case m: Counter => m.dec(m.getCount)
      case other => ()
    }
  }

  def publish(
    mr: MetricRegistry,
    toAppdynMetric: (String, Metric) => AppdynMetric,
    afterPublish: Task[Unit]
  ): Process[Task, Either[Throwable, Unit]] = {
    val schedule = time.awakeEvery(1 minute)(strategy, scheduler)
    def metrics = mr.getMetrics.asScala.toMap
    schedule >> {
      val ms = metrics.toList.map(toAppdynMetric.tupled)
      val req = Request(method = Method.POST, uri = metricsEndpointUri).withBody(ms.asJson)
      val sendMetrics = client.fetch[Unit](req)(_ => Task.now(()))
      val resetMetrics = reset(mr)
      val log = Task delay logger.info("publish 1 min metrics to appdyn")
      Process eval (sendMetrics.attempt >> afterPublish >> log).attempt.map(_.toEither)
    }
  }


}
