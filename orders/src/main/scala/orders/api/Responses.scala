package orders.api

import eventjournal.entity.OrderId
import zio.json.{DeriveJsonEncoder, JsonEncoder}

case class OrderIdResponse(orderId: OrderId)

object OrderIdResponse {
  implicit val encoder: JsonEncoder[OrderIdResponse] = DeriveJsonEncoder.gen[OrderIdResponse]
}
