package coffeeshop.api

import coffeeshop.domain.{OrdersRepo, OrdersService}
import coffeeshop.entity.{BeanOrigin, CoffeeType}
import zhttp.http
import zhttp.http.*
import zio.*
import zio.json.*
import zio.json.internal.RetractReader

import java.util.UUID
import scala.util.Try

case class ApiOrderCommand(coffeeType: String, beanOrigin: BeanOrigin)

object ApiOrderCommand {
  implicit val decoder: JsonDecoder[ApiOrderCommand] = DeriveJsonDecoder.gen[ApiOrderCommand]
}

object OrdersCommandApi {
  def apply(): HttpApp[OrdersService, Throwable] =
    Http.collectZIO[Request] {
      case req@Method.POST -> !! / "orders" =>
        req.body.asString
          .map(_.fromJson[ApiOrderCommand])
          .flatMap {
            case Right(order) => OrdersService.placeOrder(order.coffeeType, order.beanOrigin).map(id => Response.text(id.toString))
            case Left(err) => ZIO.succeed(Response.text(err))
          }
    }
}
