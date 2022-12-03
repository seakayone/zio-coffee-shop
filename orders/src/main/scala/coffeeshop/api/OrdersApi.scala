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

object OrdersApi {

  def apply(): HttpApp[OrdersService & OrdersRepo, Throwable] =
    Http.collectZIO[Request] {
      case req@Method.GET -> !! / "orders" / id =>
        ZIO.attempt(UUID.fromString(id))
          .flatMap(it => OrdersRepo.findBy(it))
          .map {
            case Some(order) => Response.text(order.toString)
            case None => Response.text("Not found")
          }
      case req@Method.GET -> !! / "orders" => OrdersRepo.findAll().map(list => Response.text(list.toString))
      case req@Method.POST -> !! / "orders" =>
        req.body.asString
          .map(_.fromJson[ApiOrder])
          .flatMap {
            case Right(order) => OrdersService.placeOrder(order.coffeeType, order.beanOrigin).map(id => Response.text(id.toString))
            case Left(err) => ZIO.succeed(Response.text(err))
          }
    }
}

case class ApiOrder(coffeeType: String, beanOrigin: BeanOrigin)

object ApiOrder {
  implicit val decoder: JsonDecoder[ApiOrder] = DeriveJsonDecoder.gen[ApiOrder]
}
