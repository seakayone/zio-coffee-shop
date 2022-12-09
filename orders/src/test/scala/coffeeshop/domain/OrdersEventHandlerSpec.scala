package coffeeshop.domain

import coffeeshop.entity.*
import coffeeshop.store.EventJournal
import zio.test.*
import zio.{Clock, ZIO, ZLayer}

import java.util.UUID
object OrdersEventHandlerSpec extends ZIOSpecDefault {
  override def spec: Spec[Any, Any] = suite("foo")(test("bar") {
    for {
      journal <- ZIO.service[EventJournal]
      now     <- Clock.instant
      event    = OrderPlaced(now, OrderInfo(UUID.randomUUID(), CoffeeType.Espresso, "Blue Mountain"))
      _       <- journal.append(event)
      
    } yield assertTrue(false)
  }).provide(OrdersEventHandler.layer, OrdersRepo.layer, EventJournal.layer)
}
