package eventjournal.entity

import zio.ZIO

enum CoffeeType {
  case Espresso, Americano, FlatWhite
}

object CoffeeType {
  def of(str: String): Option[CoffeeType] =
    CoffeeType.values.find(t => t.toString.equalsIgnoreCase(str))
}
