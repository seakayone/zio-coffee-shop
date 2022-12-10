package eventjournal

import beans.api.*
import beans.domain.{BeansCommandService, BeansEventHandler, BeansInventoryRepo, BeansQueryService}
import eventjournal.Main.validateEnv
import eventjournal.api.EventJournalApi
import eventjournal.store.EventJournal
import orders.api.{OrdersCommandApi, OrdersQueryApi}
import orders.domain.{OrdersCommandService, OrdersEventHandler, OrdersRepo}
import zhttp.http.{Http, Request, Response}
import zhttp.service.Server
import zio.*
import zio.logging.{LogFormat, console}

object Main extends ZIOAppDefault {

  override val bootstrap: ZLayer[Any, Any, Unit] = Runtime.removeDefaultLoggers >>> console(LogFormat.colored)

  private val handlerRegistrations = for {
    journal       <- ZIO.service[EventJournal]
    ordersHandler <- ZIO.service[OrdersEventHandler]
    beansHandler  <- ZIO.service[BeansEventHandler]
    _             <- journal.subscribe(ordersHandler, beansHandler)
  } yield ZIO.unit

  private val server =
    Server.start(
      8080,
      OrdersCommandApi() ++ OrdersQueryApi() ++ BeansQueryApi() ++ BeansCommandApi() ++ EventJournalApi()
    )

  private val program = handlerRegistrations *> server

  def run: ZIO[Any, Throwable, Nothing] =
    program.provide(
      ZLayer.fromZIO(ZIO.succeed(Clock.ClockLive)),
      BeansCommandService.layer,
      BeansEventHandler.layer,
      BeansInventoryRepo.layer,
      BeansQueryService.layer,
      EventJournal.layer,
      OrdersEventHandler.layer,
      OrdersRepo.layer,
      OrdersCommandService.layer
    )
}
