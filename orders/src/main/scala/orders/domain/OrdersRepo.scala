package orders.domain

import eventjournal.entity.{BeanOrigin, CoffeeType, OrderId}
import zio.json.{DeriveJsonEncoder, JsonEncoder}
import zio.{Ref, UIO, ZIO, ZLayer}

import java.time.Instant

enum OrderStatus {
  case PLACED, CANCELED
}

case class Order(
  id: OrderId,
  orderPlacedAt: Instant,
  coffeeType: CoffeeType,
  beanOrigin: BeanOrigin,
  status: OrderStatus
)

object Order {
  implicit val orderJsonEncoder: JsonEncoder[Order]           = DeriveJsonEncoder.gen[Order]
  implicit val orderStatus: JsonEncoder[OrderStatus]          = DeriveJsonEncoder.gen[OrderStatus]
  implicit val coffeeTypeJsonEncoder: JsonEncoder[CoffeeType] = DeriveJsonEncoder.gen[CoffeeType]
}

case class OrdersRepo(orders: Ref[Map[OrderId, Order]]) {
  def save(order: Order): UIO[Unit] =
    ZIO.logInfo(s"OrdersRepo saving $order") *> orders.updateAndGet(_ + (order.id -> order)).unit

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
