package coffeeshop.entity

import zio.ZIO

sealed trait CoffeeType

object Espresso extends CoffeeType

object Americano extends CoffeeType

object FlatWhite extends CoffeeType

object CoffeeType {
  def fromString(t: String): ZIO[Any, String, CoffeeType] = t.toLowerCase match {
    case "espresso" => ZIO.succeed(Espresso)
    case "americano" => ZIO.succeed(Americano)
    case "FlatWhite" => ZIO.succeed(FlatWhite)
    case _ => ZIO.fail(s"Unknown CoffeeType ${t}")
  }
}
