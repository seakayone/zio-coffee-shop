package coffeeshop.store

import coffeeshop.entity.*
import zio.test.*
import zio.{Ref, UIO, ZIO, ZLayer}

import java.time.Instant
import java.time.Instant.now
import java.util.UUID
import java.util.UUID.randomUUID

object EventJournalSpec extends ZIOSpecDefault {


  abstract class MockEventHandler(val lastEvent: Ref[Option[CoffeeEvent]]) extends EventHandler {
    override def handle(event: CoffeeEvent): UIO[Unit] = lastEvent.set(Some(event)).unit
  }

  class MockEventHandler1(lastEvent: Ref[Option[CoffeeEvent]]) extends MockEventHandler(lastEvent)

  class MockEventHandler2(lastEvent: Ref[Option[CoffeeEvent]]) extends MockEventHandler(lastEvent)


  object MockEventHandler {
    val layer1 = ZLayer.fromZIO(Ref.make(Option.empty[CoffeeEvent]).map(MockEventHandler1(_)))
    val layer2 = ZLayer.fromZIO(Ref.make(Option.empty[CoffeeEvent]).map(MockEventHandler2(_)))
  }

  override def spec: Spec[Any, Any] =
    suite("EventJournal should")(
      test("when adding an event return the last appended") {
        val event1 = OrderPlaced(now, OrderInfo(randomUUID, CoffeeType.Espresso, "blue-mountain"))
        val event2 = OrderPlaced(now, OrderInfo(randomUUID, CoffeeType.Americano, "blue-mountain"))
        for {
          _ <- EventJournal.append(event1) *> EventJournal.append(event2)
          actual <- EventJournal.lastAppended
        } yield assertTrue(actual == event2)
      },
      test("when adding events report correct size") {
        val event1 = OrderPlaced(now, OrderInfo(randomUUID, CoffeeType.Espresso, "blue-mountain"))
        val event2 = OrderPlaced(now, OrderInfo(randomUUID, CoffeeType.Americano, "blue-mountain"))
        for {
          _ <- EventJournal.append(event1) *> EventJournal.append(event2)
          actual <- EventJournal.size
        } yield assertTrue(actual == 2)
      },
      test("given handlers are subscribed when adding an event should send this to all handler") {
        val event = OrderPlaced(now, OrderInfo(randomUUID, CoffeeType.Espresso, "blue-mountain"))
        for {
          handler1 <- ZIO.service[MockEventHandler1]
          handler2 <- ZIO.service[MockEventHandler2]
          journal <- ZIO.service[EventJournal]
          _ <- journal.subscribe(handler1) *> journal.subscribe(handler2)
          _ <- journal.append(event)
          actual1 <- handler1.lastEvent.get
          actual2 <- handler2.lastEvent.get
        } yield assertTrue(actual1.contains(event) && actual2.contains(event))
      }
    ).provide(EventJournal.layer, MockEventHandler.layer1, MockEventHandler.layer2)
}
