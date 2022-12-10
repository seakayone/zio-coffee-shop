package beans.domain

import eventjournal.entity.*
import eventjournal.store.EventHandler
import zio.{UIO, ZIO, ZLayer}

case class BeansEventHandler(commandService: BeansCommandService) extends EventHandler {
  override def handle(event: CoffeeEvent): UIO[Unit] =
    ZIO.log(s"${this.getClass.getSimpleName} received << $event") *>
      (event match {
        case OrderPlaced(_, orderInfo) => commandService.reserveBeans(orderInfo.beanOrigin, orderInfo.orderId)
        case _                         => ZIO.unit
      })
}

object BeansEventHandler {
  val layer: ZLayer[BeansCommandService, Nothing, BeansEventHandler] =
    ZLayer.fromZIO {
      for {
        cmd <- ZIO.service[BeansCommandService]
      } yield BeansEventHandler(cmd)
    }
}
