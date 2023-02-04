package coffeeshop.web

import barista.api.*
import barista.domain.BaristaRepo
import beans.api.*
import beans.domain.{BeansCommandService, BeansQueryService}
import eventjournal.api.*
import eventjournal.store.EventJournal
import orders.api.*
import orders.domain.{OrdersCommandService, OrdersRepo}
import zio.*
import zio.http.*
import zio.http.model.*
import zio.metrics.connectors.prometheus.PrometheusPublisher

object WebServer {

  private val apis =
    MetricsApi() ++ OrdersCommandApi() ++ OrdersQueryApi() ++ BeansQueryApi() ++ BeansCommandApi() ++ EventJournalApi() ++ BaristaQueryApi()

  private val apisWithErrorHandling = apis.mapError(_ => Response.status(Status.InternalServerError))

  val make = Server.serve(apisWithErrorHandling, Some(e => ZIO.logError(e.getMessage)))
}
