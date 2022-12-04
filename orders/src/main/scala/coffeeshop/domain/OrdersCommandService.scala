package coffeeshop.domain

import coffeeshop.entity.*
import coffeeshop.store.EventJournal
import zio.*

import java.time.Instant
import java.time.Instant.now
import java.util.UUID
import java.util.UUID.randomUUID

case class OrdersService(journal: EventJournal) {
  def placeOrder(coffeeType: String, beanOrigin: BeanOrigin): ZIO[Any, Throwable, OrderId] = {
    val orderId: OrderId = randomUUID
    for {
      now      <- Clock.instant
      coffee   <- CoffeeType.fromString(coffeeType).mapError(IllegalArgumentException(_))
      orderInfo = OrderInfo(orderId: OrderId, coffee, beanOrigin)
      event     = OrderPlaced(now, orderInfo)
      _        <- journal.append(event)
    } yield orderId
  }
}

object OrdersService {

  def placeOrder(coffeeType: String, beanOrigin: BeanOrigin): ZIO[OrdersService, Throwable, OrderId] =
    ZIO.service[OrdersService].flatMap(_.placeOrder(coffeeType, beanOrigin))

  def layer = ZLayer.fromZIO(ZIO.service[EventJournal].map(OrdersService(_)))
}
