ThisBuild / scalaVersion := "3.2.1"
name := "zio-coffee-shop"

val ZioVersion = "2.0.4"
val ZioJsonVersion = "0.3.0-RC10"
val ZioHttpVersion = "2.0.0-RC11"
val ZioLoggingVersion = "2.1.5"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % ZioVersion,
  "io.d11" %% "zhttp" % ZioHttpVersion,
  "dev.zio" %% "zio-json" % ZioJsonVersion,
  "dev.zio" %% "zio-logging" % ZioLoggingVersion,
  "dev.zio" %% "zio-logging-slf4j" % ZioLoggingVersion,
  "ch.qos.logback" % "logback-classic" % "1.4.5",
  "dev.zio" %% "zio-test" % ZioVersion % Test,
  "dev.zio" %% "zio-test-sbt" % ZioVersion % Test,

)
testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
