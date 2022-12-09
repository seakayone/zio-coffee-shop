package beans.domain

import coffeeshop.entity.*
import coffeeshop.store.EventJournal
import zio.{Clock, Task, UIO, ZIO, ZLayer}

case class BeansCommandService(eventJournal: EventJournal, repo: BeansInventoryRepo, clock: Clock) {

  def storeBeans(beanOrigin: BeanOrigin, amount: Int): UIO[Unit] =
    clock.instant.flatMap(instant => eventJournal.append(BeansStored(instant, beanOrigin, amount))).unit

  def reserveBeans(beanOrigin: BeanOrigin, orderId: OrderId): UIO[Unit] =
    for {
      instant <- clock.instant
      amount  <- repo.getRemaining(beanOrigin)
      _ <- amount match
             case x if x == 0 => eventJournal.append(OrderFailedBeansNotAvailable(instant, orderId))
             case _           => eventJournal.appendAll(OrderBeansReserved(instant, orderId), BeansFetched(instant, beanOrigin))
    } yield ()
}

object BeansCommandService {

  def storeBeans(beanOrigin: BeanOrigin, amount: Int): ZIO[BeansCommandService, Nothing, Unit] =
    ZIO.service[BeansCommandService].flatMap(_.storeBeans(beanOrigin, amount))

  def reserveBeans(beanOrigin: BeanOrigin, orderId: OrderId): ZIO[BeansCommandService, Nothing, Unit] =
    ZIO.service[BeansCommandService].flatMap(_.reserveBeans(beanOrigin, orderId))

  val layer: ZLayer[Clock with BeansInventoryRepo with EventJournal, Nothing, BeansCommandService] = ZLayer.fromZIO {
    for {
      journal <- ZIO.service[EventJournal]
      repo    <- ZIO.service[BeansInventoryRepo]
      clock   <- ZIO.service[Clock]
    } yield BeansCommandService(journal, repo, clock)
  }
}
