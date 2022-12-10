package coffeeshop.domain

import coffeeshop.entity.*
import coffeeshop.store.EventJournal
import zio.*

import java.time.Instant
import java.time.Instant.now
import java.util.UUID
import java.util.UUID.randomUUID

case class OrdersCommandService(journal: EventJournal) {
  def placeOrder(coffeeType: CoffeeType, beanOrigin: BeanOrigin): UIO[OrderId] =
    for {
      orderId <- Random.nextUUID
      now     <- Clock.instant
      _       <- journal.append(OrderPlaced(now, OrderInfo(orderId, coffeeType, beanOrigin)))
    } yield orderId

  def cancelOrder(orderId: OrderId, reason: String): UIO[Unit] =
    Clock.instant.flatMap(now => journal.append(OrderCancelled(now, orderId, reason)))

}

object OrdersCommandService {

  def placeOrder(coffeeType: CoffeeType, beanOrigin: BeanOrigin): URIO[OrdersCommandService, OrderId] =
    ZIO.service[OrdersCommandService].flatMap(_.placeOrder(coffeeType, beanOrigin))

  def cancelOrder(orderId: OrderId, reason: String): URIO[OrdersCommandService, Unit] =
    ZIO.service[OrdersCommandService].flatMap(_.cancelOrder(orderId, reason))
  def layer = ZLayer.fromZIO(ZIO.service[EventJournal].map(OrdersCommandService(_)))
}
