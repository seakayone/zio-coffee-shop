package coffeeshop.api

import coffeeshop.domain.{OrdersRepo, OrdersService}
import coffeeshop.entity.{BeanOrigin, CoffeeType}
import zhttp.http
import zhttp.http.*
import zhttp.http.Method.GET
import zio.*
import zio.json.*
import zio.json.internal.RetractReader

import java.util.UUID
import scala.util.Try

object OrdersQueryApi {

  def apply(): HttpApp[OrdersService & OrdersRepo, Throwable] =
    Http.collectZIO[Request] {
      case GET -> !! / "orders" / id =>
        ZIO
          .attempt(UUID.fromString(id))
          .flatMap(it => OrdersRepo.findBy(it))
          .map {
            case Some(order) => Response.text(order.toString)
            case None        => Response.text("Not found")
          }
      case GET -> !! / "orders" =>
        OrdersRepo.findAll().map(list => Response.text(list.toString))
    }
}
