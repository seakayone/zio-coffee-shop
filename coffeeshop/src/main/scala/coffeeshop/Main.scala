package coffeeshop

import coffeeshop.api.{OrdersCommandApi, OrdersQueryApi}
import coffeeshop.domain.{OrdersEventHandler, OrdersRepo, OrdersService}
import coffeeshop.store.EventJournal
import zhttp.http.{Http, Request, Response}
import zhttp.service.Server
import zio.*
import zio.logging.{LogFormat, console}

object Main extends ZIOAppDefault {

  override val bootstrap: ZLayer[Any, Any, Unit] = Runtime.removeDefaultLoggers >>> console(LogFormat.colored)

  val ordersHandlerRegistration: ZIO[EventJournal with OrdersEventHandler, Nothing, UIO[Unit]] = for {
    handler <- ZIO.service[OrdersEventHandler]
    journal <- ZIO.service[EventJournal]
    _       <- journal.subscribe(handler)
  } yield ZIO.unit

  val server: ZIO[OrdersService with OrdersRepo, Throwable, Nothing] =
    Server.start(8080, OrdersCommandApi() ++ OrdersQueryApi())

  val program: ZIO[OrdersService with OrdersRepo with EventJournal with OrdersEventHandler, Throwable, Nothing] =
    ordersHandlerRegistration *> server

  def run =
    program.provide(OrdersEventHandler.layer, EventJournal.layer, OrdersService.layer, OrdersRepo.layer)
}
