package coffeeshop.domain

import coffeeshop.domain.OrdersRepo
import coffeeshop.entity.{CoffeeEvent, OrderCancelled, OrderFailedBeansNotAvailable, OrderPlaced}
import coffeeshop.store.{EventHandler, EventJournal}
import zio.Clock.*
import zio.{Clock, UIO, ULayer, ZIO, ZLayer}

case class OrdersEventHandler(repo: OrdersRepo, journal: EventJournal, clock: Clock) extends EventHandler {
  override def handle(event: CoffeeEvent): UIO[Unit] =
    ZIO.debug(s"Handling $event") *> {
      event match {
        case OrderPlaced(instant, orderInfo) =>
          repo.save(Order(orderInfo.orderId, instant, orderInfo.coffeeType, orderInfo.beanOrigin, OrderStatus.PLACED))
        case OrderFailedBeansNotAvailable(_, orderId) =>
          for {
            now   <- clock.instant
            order <- repo.findBy(orderId).map(_.get)
            _     <- repo.save(order.copy(status = OrderStatus.CANCELED))
            _     <- journal.append(OrderCancelled(now, orderId, s"No beans available for ${order.beanOrigin}"))
          } yield ()
        case _ => ZIO.unit
      }
    }
}

object OrdersEventHandler {
  val layer: ZLayer[Clock with EventJournal with OrdersRepo, Nothing, OrdersEventHandler] =
    ZLayer.fromZIO {
      for {
        repo    <- ZIO.service[OrdersRepo]
        journal <- ZIO.service[EventJournal]
        clock   <- ZIO.service[Clock]
      } yield OrdersEventHandler(repo, journal, clock)
    }
}
