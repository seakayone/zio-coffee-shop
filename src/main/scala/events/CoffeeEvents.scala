package events

import java.time.Instant

sealed trait CoffeeEvent {
  def instant: Instant
}
case class BeansFetched(instant: Instant, beansOrigin: BeanOrigin)             extends CoffeeEvent
case class BeansStored(instant: Instant, beansOrigin: BeanOrigin, amount: Int) extends CoffeeEvent
case class CoffeeBrewFinished(instant: Instant, orderId: OrderId)              extends CoffeeEvent
case class CoffeeBrewStarted(instant: Instant, orderId: OrderId)               extends CoffeeEvent
case class CoffeeDelivered(instant: Instant, orderId: OrderId)                 extends CoffeeEvent
case class OrderAccepted(instant: Instant, orderId: OrderId)                   extends CoffeeEvent
case class OrderBeansReserved(instant: Instant, orderId: OrderId)              extends CoffeeEvent
case class OrderCancelled(instant: Instant, orderId: OrderId, reason: String)  extends CoffeeEvent
case class OrderDelivered(instant: Instant, orderId: OrderId)                  extends CoffeeEvent
case class OrderFailedBeansNotAvailable(instant: Instant, orderId: OrderId)    extends CoffeeEvent
case class OrderFinished(instant: Instant, orderId: OrderId)                   extends CoffeeEvent
case class OrderPlaced(instant: Instant, orderInfo: OrderInfo)                 extends CoffeeEvent
case class OrderStarted(instant: Instant, orderId: OrderId)                    extends CoffeeEvent
