package domain

import eventjournal.entity.{CoffeeEvent, OrderPlaced}
import eventjournal.store.EventHandler
import zio.{UIO, ZIO}

case class BaristaService() {
  def placeOrder(coffeeEvent: CoffeeEvent): UIO[Unit] = ZIO.unit
}

case class BaristaEventHandler(baristaService: BaristaService) extends EventHandler {
  override def handle(event: CoffeeEvent): UIO[Unit] = event match
    case e: OrderPlaced => baristaService.placeOrder(e)
    case _              => ZIO.unit
}
