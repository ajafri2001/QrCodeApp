package routes

import cats.effect.*
import cats.syntax.all.*
import org.http4s.*
import org.http4s.dsl.io.*

object Routes:
    val routes: HttpRoutes[IO] =
        HttpRoutes.of[IO]:
            case GET -> Root / "health" =>
                Ok("Healthy")
