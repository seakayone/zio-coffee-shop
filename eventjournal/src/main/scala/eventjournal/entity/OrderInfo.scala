package eventjournal.entity

import java.util.UUID

type OrderId = UUID

type BeanOrigin = String
case class OrderInfo(orderId: OrderId, coffeeType: CoffeeType, beanOrigin: BeanOrigin)
