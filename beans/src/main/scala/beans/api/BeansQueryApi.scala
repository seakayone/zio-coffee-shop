package beans.api

import beans.domain.{BeansCommandService, BeansQueryService}
import coffeeshop.entity.{BeanOrigin, CoffeeType, OrderId}
import zhttp.http
import zhttp.http.*
import zio.*
import zio.json.*
import zio.json.internal.RetractReader

import java.util.UUID
import scala.util.Try

object BeansQueryApi {
  def apply(): HttpApp[BeansQueryService, Nothing] =
    Http.collectZIO[Request] { case Method.GET -> !! / "beans" =>
      BeansQueryService.storedBeans.map(m => Response.json(m.toJson))
    }
}
