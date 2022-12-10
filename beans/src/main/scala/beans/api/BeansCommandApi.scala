package beans.api

import beans.domain.BeansCommandService
import eventjournal.entity.{BeanOrigin, BeansStored, CoffeeType, OrderId}
import zhttp.http
import zhttp.http.*
import zio.*
import zio.json.*
import zio.json.internal.RetractReader

import java.util.UUID
import scala.util.Try

case class StoreBeansApiCommand(beanOrigin: BeanOrigin, amount: Int)
object StoreBeansApiCommand {
  implicit val decoder: JsonDecoder[StoreBeansApiCommand] = DeriveJsonDecoder.gen[StoreBeansApiCommand]
}
object BeansCommandApi {

  def apply(): HttpApp[BeansCommandService, Throwable] =
    Http.collectZIO[Request] { case req @ Method.POST -> !! / "beans" =>
      req.body.asString.map(_.fromJson[StoreBeansApiCommand]).flatMap {
        case Right(StoreBeansApiCommand(beanOrigin, amount)) =>
          if (beanOrigin == null && amount < 0) {
            ZIO.succeed(Response.text(s"BadRequest $beanOrigin or $amount is invalid"))
          } else {
            BeansCommandService.storeBeans(beanOrigin, amount).as(Response(Status.Accepted))
          }
        case Left(err) => ZIO.succeed(Response.text(err))
      }
    }
}
