package appdyn.metrics

import cats.data.NonEmptyList
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

object domain {

  case class MetricPath(elems: NonEmptyList[String]) extends AnyVal {
    def renderString: String = elems.toList.mkString("|")
  }
  object MetricPath {
    implicit def encoder: Encoder[MetricPath] =
      Encoder.encodeString.contramap(_.renderString)
  }

  sealed trait AggregatorType
  case object Sum extends AggregatorType
  case object Average extends AggregatorType
  case object Observation extends AggregatorType
  object AggregatorType {
    implicit def encoder: Encoder[AggregatorType] =
      Encoder.encodeString.contramap {
        case Sum         => "SUM"
        case Average     => "AVERAGE"
        case Observation => "OBSERVATION"
      }
  }

  case class Metric(metricName: MetricPath, aggregatorType: AggregatorType, value: Long)
  object Metric {
    implicit def encoder: Encoder[Metric] = deriveEncoder[Metric]
  }
}
