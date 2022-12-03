package coffeeshop

import coffeeshop.api.{OrdersCommandApi, OrdersQueryApi}
import coffeeshop.domain.{OrdersRepo, OrdersService}
import store.EventJournal
import zhttp.service.Server
import zio.*
import zio.logging.{LogFormat, console}

object Main extends ZIOAppDefault {

  override val bootstrap = Runtime.removeDefaultLoggers >>> console(LogFormat.colored)

  def run = ZIO.logInfo("Starting up") *>
    Server
      .start(8080, OrdersCommandApi() ++ OrdersQueryApi())
      .provide(EventJournal.layer, OrdersService.layer, OrdersRepo.layer)
}
