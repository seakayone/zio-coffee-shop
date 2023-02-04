ThisBuild / scalaVersion := "3.2.2"
name                     := "zio-coffee-shop"

val ZioVersion        = "2.0.6"
val ZioJsonVersion    = "0.4.2"
val ZioHttpVersion    = "0.0.4"
val ZioLoggingVersion = "2.1.8"
val ZioPreludeVersion = "1.0.0-RC16"
val ZioMetricsConnectorsVersion = "2.0.5"

val deps = Seq(
  "dev.zio" %% "zio"                    % ZioVersion,
  "dev.zio" %% "zio-http"               % ZioHttpVersion,
  "dev.zio" %% "zio-json"               % ZioJsonVersion,
  "dev.zio" %% "zio-logging"            % ZioLoggingVersion,
  "dev.zio" %% "zio-prelude"            % ZioPreludeVersion,
  "dev.zio" %% "zio-metrics-connectors" % ZioMetricsConnectorsVersion,
  "dev.zio" %% "zio-test"               % ZioVersion % Test,
  "dev.zio" %% "zio-test-sbt"           % ZioVersion % Test
)

testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
lazy val barista = (project in file("barista"))
  .settings(libraryDependencies ++= deps)
  .dependsOn(eventjournal)
lazy val coffeeshop = (project in file("coffeeshop"))
  .settings(libraryDependencies ++= deps)
  .dependsOn(barista, eventjournal, beans, orders)
lazy val eventjournal = (project in file("eventjournal"))
  .settings(libraryDependencies ++= deps)
lazy val beans = (project in file("beans"))
  .settings(libraryDependencies ++= deps)
  .dependsOn(eventjournal)
lazy val orders = (project in file("orders"))
  .settings(libraryDependencies ++= deps)
  .dependsOn(eventjournal)
