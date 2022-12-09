ThisBuild / scalaVersion := "3.2.1"
name                     := "zio-coffee-shop"

val ZioVersion        = "2.0.5"
val ZioJsonVersion    = "0.4.2"
val ZioHttpVersion    = "2.0.0-RC11"
val ZioLoggingVersion = "2.1.5"

val deps = Seq(
  "dev.zio" %% "zio"          % ZioVersion,
  "io.d11"  %% "zhttp"        % ZioHttpVersion,
  "dev.zio" %% "zio-json"     % ZioJsonVersion,
  "dev.zio" %% "zio-logging"  % ZioLoggingVersion,
  "dev.zio" %% "zio-test"     % ZioVersion % Test,
  "dev.zio" %% "zio-test-sbt" % ZioVersion % Test
)

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
lazy val barista = (project in file("barista"))
  .settings(libraryDependencies ++= deps)
  .dependsOn(eventstore)
lazy val coffeeshop = (project in file("coffeeshop"))
  .settings(libraryDependencies ++= deps)
  .dependsOn(barista, eventstore, beans, orders)
lazy val eventstore = (project in file("eventstore"))
  .settings(libraryDependencies ++= deps)
lazy val beans = (project in file("beans"))
  .settings(libraryDependencies ++= deps)
  .dependsOn(eventstore)
lazy val orders = (project in file("orders"))
  .settings(libraryDependencies ++= deps)
  .dependsOn(eventstore)
