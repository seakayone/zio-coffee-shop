package coffeeshop.store

import coffeeshop.entity.CoffeeEvent
import zio.*

trait EventHandler {
  def handle(event: CoffeeEvent): UIO[Unit]
}

case class EventJournal(journalRef: Ref[List[CoffeeEvent]], handlersRef: Ref[Set[EventHandler]]) {
  def append(event: CoffeeEvent): UIO[Unit] =
    for {
      _ <- ZIO.debug(s"Storing ${event}")
      _ <- journalRef.update(_.prepended(event))
      _ <- handlersRef.get.flatMap(l => l.foldRight(ZIO.unit)((h, acc) => acc *> h.handle(event)))
    } yield ()

  def lastAppended: UIO[CoffeeEvent] = journalRef.get.map(_.head)

  def size: UIO[Int] = journalRef.get.map(_.size)

  def subscribe(handler: EventHandler): UIO[Unit] =
    ZIO.debug(s"Subscribing ${handler.getClass.getSimpleName}") *>
      handlersRef.update(_ + handler).unit
}

object EventJournal {
  val layer: ZLayer[Any, Nothing, EventJournal] =
    ZLayer.fromZIO(
      for {
        journal  <- Ref.make(List.empty[CoffeeEvent])
        handlers <- Ref.make(Set.empty[EventHandler])
      } yield EventJournal(journal, handlers)
    )

  def append(event: CoffeeEvent): ZIO[EventJournal, Nothing, Unit] = ZIO.service[EventJournal].flatMap(_.append(event))

  def lastAppended: ZIO[EventJournal, Nothing, CoffeeEvent] = ZIO.service[EventJournal].flatMap(_.lastAppended)

  def size: ZIO[EventJournal, Nothing, Int] = ZIO.service[EventJournal].flatMap(_.size)

  def subscribe(handler: EventHandler) = ZIO.service[EventJournal].flatMap(_.subscribe(handler))
}
