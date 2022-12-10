package beans.domain

import eventjournal.entity.BeanOrigin
import zio.{Ref, UIO, ZIO, ZLayer}

case class BeansInventoryRepo(beansStorage: Ref[Map[BeanOrigin, Int]]) {

  def storeBeans(origin: BeanOrigin, amount: Int): UIO[Unit] = ZIO.logInfo(s"Storing additional $amount for $origin") *>
    beansStorage
      .getAndUpdate(store =>
        store.get(origin) match {
          case Some(existing) => store + (origin -> (existing + amount))
          case None           => store + (origin -> amount)
        }
      )
      .unit

  def fetchBeans(origin: BeanOrigin, amount: Int): UIO[Unit] = ZIO.logInfo(s"Getting $amount for $origin") *>
    beansStorage
      .getAndUpdate(store =>
        store.get(origin) match {
          case Some(existing) if existing >= amount => store + (origin -> (existing - amount))
          case _                                    => throw IllegalStateException(s"Not enough in store for $origin")
        }
      )
      .unit

  def getRemaining(origin: BeanOrigin): UIO[Int] = beansStorage.get.map(_.getOrElse(origin, 0))

  def storedBeans: UIO[Map[BeanOrigin, Int]] = beansStorage.get
}

object BeansInventoryRepo {

  def storeBeans(origin: BeanOrigin, amount: Int): ZIO[BeansInventoryRepo, Nothing, Unit] =
    ZIO.service[BeansInventoryRepo].flatMap(_.storeBeans(origin, amount))

  def fetchBeans(origin: BeanOrigin, amount: Int): ZIO[BeansInventoryRepo, Nothing, Unit] =
    ZIO.service[BeansInventoryRepo].flatMap(_.fetchBeans(origin, amount))

  def getRemaining(origin: BeanOrigin): ZIO[BeansInventoryRepo, Nothing, Int] =
    ZIO.service[BeansInventoryRepo].flatMap(_.getRemaining(origin))

  val layer: ZLayer[Any, Nothing, BeansInventoryRepo] =
    ZLayer.fromZIO(Ref.make(Map.empty[BeanOrigin, Int]).map(BeansInventoryRepo(_)))
}
