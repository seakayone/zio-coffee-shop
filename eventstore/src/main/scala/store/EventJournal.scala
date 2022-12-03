package store

import coffeeshop.entity.CoffeeEvent
import zio.*

case class EventJournal(journalRef: Ref[List[CoffeeEvent]]) {
  def append(event: CoffeeEvent): UIO[Unit] = journalRef.getAndUpdate(_.prepended(event)).unit

  def lastAppended: UIO[CoffeeEvent] = journalRef.get.map(_.head)

  def size: UIO[Int] = journalRef.get.map(_.size)
}

object EventJournal {
  val layer: ZLayer[Any, Nothing, EventJournal] =
    ZLayer.fromZIO(Ref.make(List.empty[CoffeeEvent]).map(ref => EventJournal(ref)))

  def append(event: CoffeeEvent): ZIO[EventJournal, Nothing, Unit] = ZIO.service[EventJournal].flatMap(_.append(event))

  def lastAppended: ZIO[EventJournal, Nothing, CoffeeEvent] = ZIO.service[EventJournal].flatMap(_.lastAppended)

  def size: ZIO[EventJournal, Nothing, Int] = ZIO.service[EventJournal].flatMap(_.size)
}
