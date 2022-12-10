package beans.domain

import eventjournal.entity.*
import eventjournal.store.EventPublisher
import zio.{Clock, Task, UIO, ZIO, ZLayer}

case class BeansCommandService(publisher: EventPublisher, repo: BeansRepo) {

  def storeBeans(beanOrigin: BeanOrigin, amount: Int): UIO[Unit] =
    for {
      now <- Clock.instant
      _   <- repo.storeBeans(beanOrigin, amount)
      _   <- publisher.append(BeansStored(now, beanOrigin, amount))
    } yield ()

  def reserveBeans(beanOrigin: BeanOrigin, orderId: OrderId): UIO[Unit] =
    for {
      instant <- Clock.instant
      amount  <- repo.getRemaining(beanOrigin)
      _ <- amount match
             case x if x <= 0 => publisher.append(OrderFailedBeansNotAvailable(instant, orderId, beanOrigin))
             case _           => fetchBeans(beanOrigin, orderId)
    } yield ()

  private def fetchBeans(beanOrigin: BeanOrigin, orderId: OrderId): UIO[Unit] =
    for {
      now <- Clock.instant
      _   <- repo.fetchBeans(beanOrigin, 1)
      _   <- publisher.appendAll(BeansFetched(now, beanOrigin), OrderBeansReserved(now, orderId))
    } yield ()

}

object BeansCommandService {

  def storeBeans(beanOrigin: BeanOrigin, amount: Int): ZIO[BeansCommandService, Nothing, Unit] =
    ZIO.service[BeansCommandService].flatMap(_.storeBeans(beanOrigin, amount))

  def reserveBeans(beanOrigin: BeanOrigin, orderId: OrderId): ZIO[BeansCommandService, Nothing, Unit] =
    ZIO.service[BeansCommandService].flatMap(_.reserveBeans(beanOrigin, orderId))

  val layer: ZLayer[BeansRepo with EventPublisher, Nothing, BeansCommandService] = ZLayer.fromZIO {
    for {
      journal <- ZIO.service[EventPublisher]
      repo    <- ZIO.service[BeansRepo]
    } yield BeansCommandService(journal, repo)
  }
}
