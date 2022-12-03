package store

import coffeeshop.entity.*
import zio.test.*

import java.time.Instant
import java.time.Instant.now
import java.util.UUID
import java.util.UUID.randomUUID

object EventJournalSpec extends ZIOSpecDefault {

  override def spec: Spec[Any, Any] =
    suite("EventJournal should")(
      test("when adding an event return the last appended") {
        val event1 = OrderPlaced(now, OrderInfo(randomUUID, Espresso, "blue-mountain"))
        val event2 = OrderPlaced(now, OrderInfo(randomUUID, Americano, "blue-mountain"))
        for {
          _      <- EventJournal.append(event1) *> EventJournal.append(event2)
          actual <- EventJournal.lastAppended
        } yield assertTrue(actual == event2)
      },
      test("when adding events report correct size") {
        val event1 = OrderPlaced(now, OrderInfo(randomUUID, Espresso, "blue-mountain"))
        val event2 = OrderPlaced(now, OrderInfo(randomUUID, Americano, "blue-mountain"))
        for {
          _      <- EventJournal.append(event1) *> EventJournal.append(event2)
          actual <- EventJournal.size
        } yield assertTrue(actual == 2)
      }
    ).provide(EventJournal.layer)
}
