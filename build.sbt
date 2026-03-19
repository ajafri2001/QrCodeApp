scalaVersion := "3.8.2"

val http4sVersion = "1.0.0-M45"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-ember-client" % http4sVersion,
  "org.http4s" %% "http4s-ember-server" % http4sVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "io.nayuki" % "qrcodegen" % "1.8.0",
  "org.typelevel" %% "cats-effect" % "3.7.0",
  "org.typelevel" %% "log4cats-slf4j" % "2.8.0"
)
