package coffeeshop.web

import zio.*
import zio.http.*
import zio.http.model.*
import zio.metrics.connectors.prometheus.PrometheusPublisher
object MetricsApi {
  def apply(): App[PrometheusPublisher] =
    Http
      .collectZIO[Request] { case Method.GET -> !! / "metrics" =>
        ZIO.serviceWithZIO[PrometheusPublisher](_.get.map(Response.text))
      }
}
