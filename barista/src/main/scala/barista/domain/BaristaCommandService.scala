package barista.domain

import eventjournal.entity.*
import eventjournal.store.EventPublisher
import zio.{Clock, Duration, Random, UIO, ZIO, ZLayer}

case class BaristaCommandService(publisher: EventPublisher, repo: BaristaRepo) {

  private def updateStatus(orderId: OrderId, newStatus: BrewStatus): UIO[Brew] =
    for {
      brew <- repo
                .findBy(orderId)
                .map(_.getOrElse(throw new IllegalStateException(s"Brew for order $orderId not found, should exist")))
      newBrew = brew.copy(status = newStatus)
      _      <- repo.save(orderId, newBrew)
    } yield newBrew

  private def wait(reason: String, minInclusive: Int, maxExclusive: Int) =
    for {
      waitForSeconds <- Random.nextIntBetween(minInclusive, maxExclusive)
      _              <- ZIO.logInfo(s"$reason takes time: $waitForSeconds s")
      _              <- ZIO.sleep(Duration.fromSeconds(waitForSeconds))
    } yield ()

  def makeCoffee(orderInfo: OrderInfo): UIO[Unit] =
    for {
      now <- Clock.instant
      _   <- publisher.append(CoffeeBrewStarted(now, orderInfo.orderId))
      _   <- repo.save(orderInfo.orderId, Brew(orderInfo.coffeeType, orderInfo.beanOrigin, BrewStatus.STARTED))
      _   <- finishCoffee(orderInfo.orderId).forkDaemon
    } yield ()

  private def finishCoffee(orderId: OrderId) =
    for {
      _   <- wait(s"Finishing Coffee $orderId", 30, 120)
      now <- Clock.instant
      _   <- publisher.append(CoffeeBrewFinished(now, orderId))
      _   <- updateStatus(orderId, BrewStatus.FINISHED)
      _   <- deliverCoffee(orderId).forkDaemon
    } yield ()

  private def deliverCoffee(orderId: OrderId) = for {
    _   <- wait(s"Delivering Coffee $orderId", 5, 10)
    now <- Clock.instant
    _   <- publisher.append(CoffeeDelivered(now, orderId))
    _   <- updateStatus(orderId, BrewStatus.DELIVERED)
  } yield ()
}

object BaristaCommandService {

  def makeCoffee(orderInfo: OrderInfo): ZIO[BaristaCommandService, Nothing, Unit] =
    ZIO.service[BaristaCommandService].flatMap(_.makeCoffee(orderInfo))

  val layer: ZLayer[BaristaRepo with EventPublisher, Nothing, BaristaCommandService] =
    ZLayer.fromZIO(for {
      journal <- ZIO.service[EventPublisher]
      repo    <- ZIO.service[BaristaRepo]
    } yield BaristaCommandService(journal, repo))
}
