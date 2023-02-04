package coffeeshop.infrastructure

import barista.domain.BaristaEventHandler
import beans.domain.BeansEventHandler
import eventjournal.store.EventJournal
import orders.domain.OrdersEventHandler
import zio.*
object HandlerRegistrations {

  val make
    : ZIO[BaristaEventHandler with BeansEventHandler with OrdersEventHandler with EventJournal, Nothing, UIO[Unit]] =
    for {
      journal        <- ZIO.service[EventJournal]
      ordersHandler  <- ZIO.service[OrdersEventHandler]
      beansHandler   <- ZIO.service[BeansEventHandler]
      baristaHandler <- ZIO.service[BaristaEventHandler]
      _              <- journal.subscribe(ordersHandler, beansHandler, baristaHandler)
    } yield ZIO.unit

}
