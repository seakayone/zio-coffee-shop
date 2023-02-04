package coffeeshop.main

import barista.api.BaristaQueryApi
import barista.domain.{BaristaCommandService, BaristaEventHandler, BaristaRepo}
import beans.api.*
import beans.domain.{BeansCommandService, BeansEventHandler, BeansQueryService, BeansRepo}
import coffeeshop.infrastructure.HandlerRegistrations
import coffeeshop.main.Main.validateEnv
import coffeeshop.web.{MetricsApi, WebServer}
import eventjournal.api.EventJournalApi
import eventjournal.store.EventJournal
import orders.api.{OrdersCommandApi, OrdersQueryApi}
import orders.domain.{OrdersCommandService, OrdersEventHandler, OrdersRepo}
import zio.*
import zio.http.*
import zio.http.model.*
import zio.logging.{LogFormat, console}
import zio.metrics.connectors.prometheus.PrometheusPublisher
import zio.metrics.connectors.{MetricsConfig, prometheus}
import zio.metrics.jvm.DefaultJvmMetrics

object Main extends ZIOAppDefault {

  override val bootstrap: ZLayer[Any, Any, Unit] = Runtime.removeDefaultLoggers >>> console(LogFormat.colored)

  private val program = HandlerRegistrations.make *> WebServer.make

  def run: ZIO[Any, Throwable, Nothing] =
    program.provide(
      // events
      EventJournal.layer,
      // barista
      BaristaEventHandler.layer,
      BaristaRepo.layer,
      BaristaCommandService.layer,
      // beans
      BeansCommandService.layer,
      BeansEventHandler.layer,
      BeansRepo.layer,
      BeansQueryService.layer,
      // orders
      OrdersEventHandler.layer,
      OrdersRepo.layer,
      OrdersCommandService.layer,
      /// metrics
      ZLayer.succeed(MetricsConfig(5.seconds)),
      prometheus.publisherLayer,
      prometheus.prometheusLayer,
      DefaultJvmMetrics.live.unit,
      // HTTP
      ServerConfig.live >>> Server.live
    )
}
