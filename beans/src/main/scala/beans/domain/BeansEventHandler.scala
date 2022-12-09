package beans.domain

import coffeeshop.entity.*
import coffeeshop.store.{EventHandler, EventJournal}
import zio.{UIO, ZIO, ZLayer}

case class BeansEventHandler(repo: BeansInventoryRepo, cmd: BeansCommandService) extends EventHandler {
  override def handle(event: CoffeeEvent): UIO[Unit] = event match {
    case OrderPlaced(_, orderInfo)           => cmd.reserveBeans(orderInfo.beanOrigin, orderInfo.orderId)
    case BeansStored(_, beansOrigin, amount) => repo.storeBeans(beansOrigin, amount)
    case BeansFetched(_, beansOrigin)        => repo.fetchBeans(beansOrigin, 1)
    case _                                   => ZIO.unit
  }
}

object BeansEventHandler {
  val layer: ZLayer[EventJournal with BeansCommandService with BeansInventoryRepo, Nothing, BeansEventHandler] =
    ZLayer.fromZIO {
      for {
        repo    <- ZIO.service[BeansInventoryRepo]
        cmd     <- ZIO.service[BeansCommandService]
        journal <- ZIO.service[EventJournal]
        handler  = BeansEventHandler(repo, cmd)
        _       <- journal.subscribe(handler)
      } yield handler
    }
}
