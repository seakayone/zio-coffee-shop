package coffeeshop.domain

import coffeeshop.entity.{CoffeeEvent, OrderPlaced}
import coffeeshop.store.{EventHandler, EventJournal}
import zio.{UIO, ULayer, ZIO, ZLayer}

case class OrdersEventHandler(repo: OrdersRepo) extends EventHandler {
  override def handle(event: CoffeeEvent): UIO[Unit] =
    ZIO.debug(s"Handling $event") *> {
      event match {
        case OrderPlaced(instant, orderInfo) =>
          repo.save(Order(orderInfo.orderId, instant, orderInfo.coffeeType, orderInfo.beanOrigin))
        case _ => ZIO.unit
      }
    }
}

object OrdersEventHandler {
  val layer: ZLayer[OrdersRepo, Nothing, OrdersEventHandler] =
    ZLayer.fromZIO(ZIO.service[OrdersRepo].map(OrdersEventHandler(_)))
}
