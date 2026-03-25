import scala.sys.process.*

scalaVersion := "3.8.2"

val http4sVersion = "1.0.0-M45"
val circeVersion  = "0.14.15"

libraryDependencies ++= Seq(
    "org.http4s"                            %% "http4s-ember-server"   % http4sVersion,
    "org.http4s"                            %% "http4s-dsl"            % http4sVersion,
    "io.nayuki"                              % "qrcodegen"             % "1.8.0",
    "org.typelevel"                         %% "cats-effect"           % "3.7.0",
    "org.typelevel"                         %% "log4cats-slf4j"        % "2.8.0",
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"   % "2.38.9",
    "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.38.9" % "compile-internal"
)

lazy val buildFrontend = taskKey[Unit]("Build frontend")

buildFrontend := {
    val frontend = baseDirectory.value / "frontend"
    val log      = streams.value.log
    val target   = (Compile / resourceDirectory).value / "dist"

    log.info("Building frontend...")

    Process("npm install", frontend).!
    Process("npm run build", frontend).!

    IO.delete(target)
    IO.copyDirectory(frontend / "dist", target)

    log.info("Frontend copied to resources/dist")
}

lazy val build = taskKey[Unit]("Build frontend and compile backend")

build := Def.sequential(
    buildFrontend,
    Compile / compile
).value
