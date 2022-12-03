package coffeeshop.entity

sealed trait CoffeeType
object Espresso  extends CoffeeType
object Americano extends CoffeeType
object FlatWhite extends CoffeeType
