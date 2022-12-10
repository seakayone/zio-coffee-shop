package orders.domain

import eventjournal.entity.*
import eventjournal.store.*
import orders.domain.*
import zio.*
import zio.Clock.*

case class OrdersEventHandler(repo: OrdersRepo, commandService: OrdersCommandService) extends EventHandler {
  override def handle(event: CoffeeEvent): UIO[Unit] =
    ZIO.logInfo(s"OrdersEventHandler received << $event") *> {
      event match {
        case OrderPlaced(instant, orderInfo) =>
          repo.save(Order(orderInfo.orderId, instant, orderInfo.coffeeType, orderInfo.beanOrigin, OrderStatus.PLACED))
        case OrderFailedBeansNotAvailable(_, orderId) =>
          for {
            order <- repo.findBy(orderId).map(_.get)
            _     <- repo.save(order.copy(status = OrderStatus.CANCELED))
            _     <- commandService.cancelOrder(orderId, s"No beans available for ${order.beanOrigin}")
          } yield ()
        case _ => ZIO.unit
      }
    }
}

object OrdersEventHandler {
  val layer: ZLayer[OrdersCommandService with OrdersRepo, Nothing, OrdersEventHandler] =
    ZLayer.fromZIO {
      for {
        repo           <- ZIO.service[OrdersRepo]
        commandService <- ZIO.service[OrdersCommandService]
      } yield OrdersEventHandler(repo, commandService)
    }
}
