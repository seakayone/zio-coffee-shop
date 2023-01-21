package orders.api

import eventjournal.entity.{BeanOrigin, CoffeeType}
import orders.domain.{OrdersCommandService, OrdersRepo}
import zio.http.*
import zio.http.model.*
import zio.*
import zio.json.*
import zio.json.internal.RetractReader

import java.util.UUID
import scala.util.Try

object OrdersQueryApi {

  def apply(): HttpApp[OrdersCommandService & OrdersRepo, Throwable] =
    Http.collectZIO[Request] {
      case Method.GET -> !! / "orders" / id =>
        ZIO
          .attempt(UUID.fromString(id))
          .flatMap(it => OrdersRepo.findBy(it))
          .map {
            case Some(order) => Response.json(order.toJson)
            case None        => Response.text("Not found")
          }
      case Method.GET -> !! / "orders" =>
        OrdersRepo.findAll().map(list => Response.json(list.toJson))
    }
}
