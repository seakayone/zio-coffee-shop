package eventjournal

import barista.api.BaristaQueryApi
import barista.domain.{BaristaCommandService, BaristaEventHandler, BaristaRepo}
import beans.api.*
import beans.domain.{BeansCommandService, BeansEventHandler, BeansQueryService, BeansRepo}
import eventjournal.Main.validateEnv
import eventjournal.api.EventJournalApi
import eventjournal.store.EventJournal
import orders.api.{OrdersCommandApi, OrdersQueryApi}
import orders.domain.{OrdersCommandService, OrdersEventHandler, OrdersRepo}
import zio.*
import zio.http.*
import zio.http.model.*
import zio.logging.{LogFormat, console}

object Main extends ZIOAppDefault {

  override val bootstrap: ZLayer[Any, Any, Unit] = Runtime.removeDefaultLoggers >>> console(LogFormat.colored)

  private val handlerRegistrations = for {
    journal        <- ZIO.service[EventJournal]
    ordersHandler  <- ZIO.service[OrdersEventHandler]
    beansHandler   <- ZIO.service[BeansEventHandler]
    baristaHandler <- ZIO.service[BaristaEventHandler]
    _              <- journal.subscribe(ordersHandler, beansHandler, baristaHandler)
  } yield ZIO.unit

  private val errorCallback: Throwable => ZIO[Any, Nothing, Unit] =
    e => ZIO.logError(e.getMessage)

  private val server =
    Server.serve(
      OrdersCommandApi() ++ OrdersQueryApi() ++ BeansQueryApi() ++ BeansCommandApi() ++ EventJournalApi() ++ BaristaQueryApi(),
      Some(errorCallback)
    )

  private object HttpServer {
    val layer: ZLayer[Any, Throwable, Server] = ServerConfig.live >>> Server.live
  }

  private val program = handlerRegistrations *> server

  def run: ZIO[Any, Throwable, Nothing] =
    program.provide(
      BaristaEventHandler.layer,
      BaristaRepo.layer,
      BaristaCommandService.layer,
      BeansCommandService.layer,
      BeansEventHandler.layer,
      BeansRepo.layer,
      BeansQueryService.layer,
      EventJournal.layer,
      HttpServer.layer,
      OrdersEventHandler.layer,
      OrdersRepo.layer,
      OrdersCommandService.layer
    )
}
