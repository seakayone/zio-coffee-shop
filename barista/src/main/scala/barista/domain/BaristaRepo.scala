package barista.domain

import eventjournal.entity.*
import zio.json.{DeriveJsonEncoder, JsonEncoder}
import zio.{Ref, UIO, ZIO, ZLayer}

import java.util.UUID

enum BrewStatus {
  case STARTED, FINISHED, DELIVERED
}
case class Brew(coffeeType: CoffeeType, beanOrigin: BeanOrigin, status: BrewStatus)
object Brew {
  implicit val brewStatusEncoder: JsonEncoder[BrewStatus] = DeriveJsonEncoder.gen[BrewStatus]
  implicit val coffeeTypeEncoder: JsonEncoder[CoffeeType] = DeriveJsonEncoder.gen[CoffeeType]
  implicit val brewEncoder: JsonEncoder[Brew]             = DeriveJsonEncoder.gen[Brew]
}

case class BaristaRepo(brews: Ref[Map[OrderId, Brew]]) {

  def findBy(orderId: OrderId): UIO[Option[Brew]] = brews.get.map(_.get(orderId))

  def save(orderId: OrderId, brew: Brew): UIO[Unit] =
    ZIO.logInfo(s"BaristaRepo saving $brew") *> brews.updateAndGet(_ + (orderId -> brew)).unit

  def findAll(): UIO[Map[OrderId, Brew]] = brews.get
}

object BaristaRepo {
  def findAll: ZIO[BaristaRepo, Nothing, Map[OrderId, Brew]] =
    ZIO.service[BaristaRepo].flatMap(_.findAll())

  val layer: ZLayer[Any, Nothing, BaristaRepo] = ZLayer.fromZIO(Ref.make(Map.empty[OrderId, Brew]).map(BaristaRepo(_)))
}
