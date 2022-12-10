package orders.domain

import eventjournal.entity.*
import eventjournal.store.*
import orders.domain.*
import orders.domain.OrderStatus.{ACCEPTED, CANCELLED, PLACED}
import zio.*
import zio.Clock.*

case class OrdersEventHandler(repo: OrdersRepo, orders: OrdersCommandService) extends EventHandler {

  override def handle(event: CoffeeEvent): UIO[Unit] =
    ZIO.logInfo(s"OrdersEventHandler received << $event") *> {
      event match {
        case CoffeeDelivered(_, orderId)    => orders.deliver(orderId)
        case CoffeeBrewFinished(_, orderId) => orders.finish(orderId)
        case CoffeeBrewStarted(_, orderId)  => orders.start(orderId)
        case OrderBeansReserved(_, orderId) => orders.accept(orderId)
        case OrderFailedBeansNotAvailable(_, orderId, origin) =>
          orders.cancel(orderId, s"No beans available for $origin")
        case _ => ZIO.unit
      }
    }
}

object OrdersEventHandler {

  def handle(event: CoffeeEvent): ZIO[OrdersEventHandler, Nothing, Unit] =
    ZIO.service[OrdersEventHandler].flatMap(_.handle(event))

  val layer: ZLayer[OrdersCommandService with OrdersRepo, Nothing, OrdersEventHandler] =
    ZLayer.fromZIO {
      for {
        repo           <- ZIO.service[OrdersRepo]
        commandService <- ZIO.service[OrdersCommandService]
      } yield OrdersEventHandler(repo, commandService)
    }
}
