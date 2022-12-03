package coffeeshop.domain

import coffeeshop.entity.*
import store.EventJournal
import zio.*

import java.time.Instant
import java.time.Instant.now
import java.util.UUID
import java.util.UUID.randomUUID

case class OrdersService(journal: EventJournal, ordersRepo: OrdersRepo) {
  def placeOrder(coffeeType: String, beanOrigin: BeanOrigin): ZIO[Any, Throwable, OrderId] = {
    val orderId: OrderId = randomUUID
    for {
      now <- Clock.instant
      coffee <- CoffeeType.fromString(coffeeType).mapError(IllegalArgumentException(_))
      orderInfo = OrderInfo(orderId: OrderId, coffee, beanOrigin)
      event = OrderPlaced(now, orderInfo)
      _ <- journal.append(event)
      // For now store directly in the repo,
      // TODO: should be replaced by an event listener
      _ <- ordersRepo.save(Order(orderInfo.orderId, event.instant, orderInfo.coffeeType, orderInfo.beanOrigin))
    } yield orderId
  }
}

object OrdersService {

  def placeOrder(coffeeType: String, beanOrigin: BeanOrigin): ZIO[OrdersService, Throwable, OrderId] =
    ZIO.service[OrdersService].flatMap(_.placeOrder(coffeeType, beanOrigin))

  def layer = ZLayer.fromZIO {
    for {
      journal <- ZIO.service[EventJournal]
      repo <- ZIO.service[OrdersRepo]
    } yield OrdersService(journal, repo)
  }
}
