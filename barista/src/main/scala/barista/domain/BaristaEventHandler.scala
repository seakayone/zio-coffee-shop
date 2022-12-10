package barista.domain

import eventjournal.entity.{CoffeeEvent, OrderAccepted, OrderPlaced}
import eventjournal.store.EventHandler
import zio.{UIO, ZIO, ZLayer}

case class BaristaEventHandler(barista: BaristaCommandService) extends EventHandler {
  override def handle(event: CoffeeEvent): UIO[Unit] =
    ZIO.logInfo(s"BaristaEventHandler received << $event") *>
      (event match
        case OrderAccepted(_, orderInfo) => barista.makeCoffee(orderInfo)
        case _                           => ZIO.unit
      )
}

object BaristaEventHandler {
  val layer: ZLayer[BaristaCommandService, Nothing, BaristaEventHandler] =
    ZLayer.fromZIO {
      for {
        barista <- ZIO.service[BaristaCommandService]
      } yield BaristaEventHandler(barista)
    }
}
