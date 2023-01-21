package beans.api

import beans.domain.BeansQueryService
import eventjournal.entity.{BeanOrigin, CoffeeType, OrderId}
import zio.http.*
import zio.http.model.*
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
