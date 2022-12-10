package coffeeshop.api

import coffeeshop.domain.{OrdersRepo, OrdersCommandService}
import coffeeshop.entity.{BeanOrigin, CoffeeType, OrderId}
import zhttp.http
import zhttp.http.*
import zhttp.http.HttpError.BadRequest
import zio.*
import zio.json.*
import zio.json.internal.RetractReader

import java.util.UUID
import scala.util.Try

case class OrderApiCommand(coffeeType: String, beanOrigin: BeanOrigin)

object OrderApiCommand {
  implicit val decoder: JsonDecoder[OrderApiCommand] = DeriveJsonDecoder.gen[OrderApiCommand]
}

case class OrderPlacedResponse(orderId: OrderId, action: String = "orderPlaced")

object OrderPlacedResponse {
  implicit val encoder: JsonEncoder[OrderPlacedResponse] = DeriveJsonEncoder.gen[OrderPlacedResponse]
}

object OrdersCommandApi {
  def apply(): HttpApp[OrdersCommandService, Throwable] =
    Http.collectZIO[Request] { case req @ Method.POST -> !! / "orders" =>
      req.body.asString
        .map(_.fromJson[OrderApiCommand])
        .flatMap {
          case Right(order) =>
            for {
              coffeeType <- CoffeeType.fromString(order.coffeeType).mapError(BadRequest(_))
              id         <- OrdersCommandService.placeOrder(coffeeType, order.beanOrigin)
            } yield Response.json(OrderPlacedResponse(id).toJson)
          case Left(err) => ZIO.succeed(Response.text(err))
        }
    }
}
