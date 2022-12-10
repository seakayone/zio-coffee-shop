package beans.domain

import eventjournal.entity.*
import eventjournal.store.{EventHandler, EventJournal}
import zio.{UIO, ZIO, ZLayer}

case class BeansEventHandler(repo: BeansInventoryRepo, commandService: BeansCommandService) extends EventHandler {
  override def handle(event: CoffeeEvent): UIO[Unit] =
    ZIO.log(s"${this.getClass.getSimpleName} received << $event") *>
      (event match {
        case OrderPlaced(_, orderInfo)           => commandService.reserveBeans(orderInfo.beanOrigin, orderInfo.orderId)
        case BeansStored(_, beansOrigin, amount) => repo.storeBeans(beansOrigin, amount)
        case BeansFetched(_, beansOrigin)        => repo.fetchBeans(beansOrigin, 1)
        case _                                   => ZIO.unit
      })
}

object BeansEventHandler {
  val layer: ZLayer[BeansCommandService with BeansInventoryRepo, Nothing, BeansEventHandler] =
    ZLayer.fromZIO {
      for {
        repo <- ZIO.service[BeansInventoryRepo]
        cmd  <- ZIO.service[BeansCommandService]
      } yield BeansEventHandler(repo, cmd)
    }
}
