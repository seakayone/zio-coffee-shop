package eventjournal.store

import eventjournal.entity.CoffeeEvent
import zio.*

trait EventHandler {
  def handle(event: CoffeeEvent): UIO[Unit]
  def name: String = this.getClass.getSimpleName
}

case class EventJournal(journalRef: Ref[List[CoffeeEvent]], handlersRef: Ref[Set[EventHandler]]) {

  def appendAll(events: CoffeeEvent*): UIO[Unit] =
    ZIO.foreachDiscard(events)(event => append(event))

  def append(event: CoffeeEvent): UIO[Unit] =
    for {
      _ <- ZIO.logInfo(s"EventJournal appending << $event")
      _ <- journalRef.update(_.prepended(event))
      _ <- handlersRef.get.flatMap(l => l.foldRight(ZIO.unit)((h, acc) => acc *> h.handle(event)))
    } yield ()

  def lastAppended: UIO[CoffeeEvent] = journalRef.get.map(_.head)

  def listAll: UIO[List[CoffeeEvent]] = journalRef.get

  def size: UIO[Int] = journalRef.get.map(_.size)

  def subscribe(handler: EventHandler*): UIO[Unit] =
    ZIO.logInfo(s"Subscribing ${handler.map(_.name).mkString(",")}") *>
      ZIO.foreach(handler)(it => handlersRef.update(_ + it)).unit
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

  def listAll: ZIO[EventJournal, Nothing, List[CoffeeEvent]] = ZIO.service[EventJournal].flatMap(_.listAll)

  def size: ZIO[EventJournal, Nothing, Int] = ZIO.service[EventJournal].flatMap(_.size)

  def subscribe(handler: EventHandler): ZIO[EventJournal, Nothing, Unit] =
    ZIO.service[EventJournal].flatMap(_.subscribe(handler))
}
