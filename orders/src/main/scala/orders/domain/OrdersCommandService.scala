package orders.domain

import eventjournal.entity.*
import eventjournal.store.EventPublisher
import orders.domain.OrderStatus.*
import zio.*

import java.time.Instant
import java.time.Instant.now
import java.util.UUID
import java.util.UUID.randomUUID

case class OrdersCommandService(publisher: EventPublisher, repo: OrdersRepo) {

  private def updateStatus(orderId: OrderId, newStatus: OrderStatus): ZIO[Any, Nothing, Order] =
    for {
      order <- repo
                 .findBy(orderId)
                 .map(_.getOrElse(throw new IllegalStateException(s"Order $orderId must be present but is not")))
      updatedOrder = order.copy(status = newStatus)
      _           <- repo.save(updatedOrder)
    } yield updatedOrder

  def placeOrder(coffeeType: CoffeeType, beanOrigin: BeanOrigin): UIO[OrderId] =
    for {
      orderId <- Random.nextUUID
      now     <- Clock.instant
      _       <- repo.save(Order(orderId, now, coffeeType, beanOrigin, PLACED))
      _       <- publisher.append(OrderPlaced(now, OrderInfo(orderId, coffeeType, beanOrigin)))
    } yield orderId

  def start(orderId: OrderId): UIO[Unit] = for {
    now <- Clock.instant
    _   <- updateStatus(orderId, STARTED)
    _   <- publisher.append(OrderStarted(now, orderId))
  } yield ()

  def finish(orderId: OrderId): UIO[Unit] = for {
    now <- Clock.instant
    _   <- updateStatus(orderId, FINISHED)
    _   <- publisher.append(OrderFinished(now, orderId))
  } yield ()

  def deliver(orderId: OrderId): UIO[Unit] = for {
    now <- Clock.instant
    _   <- updateStatus(orderId, DELIVERED)
    _   <- publisher.append(OrderDelivered(now, orderId))
  } yield ()

  def cancel(orderId: OrderId, reason: String): UIO[Unit] = for {
    now <- Clock.instant
    _   <- updateStatus(orderId, CANCELLED)
    _   <- publisher.append(OrderCancelled(now, orderId, reason))
  } yield ()

  def accept(orderId: OrderId): UIO[Unit] =
    for {
      now   <- Clock.instant
      order <- updateStatus(orderId, ACCEPTED)
      _     <- publisher.append(OrderAccepted(now, OrderInfo(order.id, order.coffeeType, order.beanOrigin)))
    } yield ()
}

object OrdersCommandService {

  def placeOrder(coffeeType: CoffeeType, beanOrigin: BeanOrigin): URIO[OrdersCommandService, OrderId] =
    ZIO.service[OrdersCommandService].flatMap(_.placeOrder(coffeeType, beanOrigin))

  def cancelOrder(orderId: OrderId, reason: String): URIO[OrdersCommandService, Unit] =
    ZIO.service[OrdersCommandService].flatMap(_.cancel(orderId, reason))

  def acceptOrder(orderId: OrderId): URIO[OrdersCommandService, Unit] =
    ZIO.service[OrdersCommandService].flatMap(_.accept(orderId))

  def layer: ZLayer[EventPublisher with OrdersRepo, Nothing, OrdersCommandService] =
    ZLayer.fromZIO {
      for {
        journal <- ZIO.service[EventPublisher]
        repo    <- ZIO.service[OrdersRepo]
      } yield OrdersCommandService(journal, repo)
    }
}
