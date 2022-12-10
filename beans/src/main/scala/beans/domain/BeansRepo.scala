package beans.domain

import eventjournal.entity.BeanOrigin
import zio.{Ref, UIO, ZIO, ZLayer}

case class BeansRepo(beansStorage: Ref[Map[BeanOrigin, Int]]) {

  def storeBeans(origin: BeanOrigin, amount: Int): UIO[Unit] =
    beansStorage
      .getAndUpdate(store =>
        store.get(origin) match {
          case Some(existing) => store + (origin -> (existing + amount))
          case None           => store + (origin -> amount)
        }
      )
      .unit

  def fetchBeans(origin: BeanOrigin, amount: Int): UIO[Unit] =
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

object BeansRepo {

  def storeBeans(origin: BeanOrigin, amount: Int): ZIO[BeansRepo, Nothing, Unit] =
    ZIO.service[BeansRepo].flatMap(_.storeBeans(origin, amount))

  def fetchBeans(origin: BeanOrigin, amount: Int): ZIO[BeansRepo, Nothing, Unit] =
    ZIO.service[BeansRepo].flatMap(_.fetchBeans(origin, amount))

  def getRemaining(origin: BeanOrigin): ZIO[BeansRepo, Nothing, Int] =
    ZIO.service[BeansRepo].flatMap(_.getRemaining(origin))

  val layer: ZLayer[Any, Nothing, BeansRepo] =
    ZLayer.fromZIO(Ref.make(Map.empty[BeanOrigin, Int]).map(BeansRepo(_)))
}
