package eventjournal.entity

import zio.ZIO

enum CoffeeType {
  case Espresso, Americano, FlatWhite
}

object CoffeeType {
  def fromString(t: String): ZIO[Any, String, CoffeeType] = t.toLowerCase match {
    case "espresso"  => ZIO.succeed(Espresso)
    case "americano" => ZIO.succeed(Americano)
    case "FlatWhite" => ZIO.succeed(FlatWhite)
    case _           => ZIO.fail(s"Unknown CoffeeType ${t}")
  }
}
