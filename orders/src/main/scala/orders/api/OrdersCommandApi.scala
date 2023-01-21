package orders.api

import eventjournal.entity.{BeanOrigin, CoffeeType, OrderId}
import orders.domain.{OrdersCommandService, OrdersRepo}
import zio.http.*
import zio.http.model.*
import zio.*
import zio.http.model.HttpError.BadRequest
import zio.json.*
import zio.json.internal.RetractReader

import java.util.UUID
import scala.util.Try

case class OrderApiCommand(coffeeType: String, beanOrigin: BeanOrigin)

object OrderApiCommand {
  implicit val decoder: JsonDecoder[OrderApiCommand] = DeriveJsonDecoder.gen[OrderApiCommand]
}

object OrdersCommandApi {
  def apply(): HttpApp[OrdersCommandService, Throwable] =
    Http.collectZIO[Request] { case req @ Method.POST -> !! / "orders" =>
      req.body.asString
        .map(_.fromJson[OrderApiCommand])
        .flatMap {
          case Right(order) =>
            for {
              coffeeType <- ZIO
                              .fromOption(CoffeeType.of(order.coffeeType))
                              .orElseFail(BadRequest(s"Unknown coffeeType ${order.coffeeType}"))
              id <- OrdersCommandService.placeOrder(coffeeType, order.beanOrigin)
            } yield Response.json(OrderIdResponse(id).toJson).setStatus(Status.Accepted)
          case Left(err) => ZIO.succeed(Response.text(err))
        }
    }
}
