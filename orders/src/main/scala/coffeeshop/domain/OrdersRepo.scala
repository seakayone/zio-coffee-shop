package coffeeshop.domain

import coffeeshop.entity.{BeanOrigin, CoffeeType, OrderId}
import zio.{Ref, UIO, ZIO, ZLayer}

import java.time.Instant

case class Order(id: OrderId, orderPlacedAt: Instant, coffeeType: CoffeeType, beanOrigin: BeanOrigin)

case class OrdersRepo(orders: Ref[Map[OrderId, Order]]) {
  def save(order: Order): UIO[Unit] = ZIO.debug(s"Saving $order") *> orders.updateAndGet(_ + (order.id -> order)).unit

  def findBy(orderId: OrderId): UIO[Option[Order]] = orders.get.map(_.get(orderId))

  def findAll(): UIO[List[Order]] = orders.get.map(_.values.toList)
}

object OrdersRepo {
  def save(order: Order): ZIO[OrdersRepo, Nothing, Unit] = ZIO.service[OrdersRepo].flatMap(_.save(order))

  def findBy(orderId: OrderId): ZIO[OrdersRepo, Nothing, Option[Order]] =
    ZIO.service[OrdersRepo].flatMap(_.findBy(orderId))

  def findAll(): ZIO[OrdersRepo, Nothing, List[Order]] = ZIO.service[OrdersRepo].flatMap(_.findAll())

  val layer: ZLayer[Any, Nothing, OrdersRepo] = ZLayer.fromZIO(Ref.make(Map.empty[OrderId, Order]).map(OrdersRepo(_)))
}
