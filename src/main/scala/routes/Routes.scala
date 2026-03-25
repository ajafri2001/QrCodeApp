package routes

import cats.effect.*
import cats.syntax.all.*
import org.http4s.*
import org.http4s.dsl.io.*
import io.circe.Json
import org.http4s.circe.CirceEntityCodec.*

object Routes:
    object DataQueryParam extends QueryParamDecoderMatcher[String]("data")

    val routes: HttpRoutes[IO] =
        HttpRoutes.of[IO]:
            case GET -> Root / "api" / "health" =>
                Ok("Healthy")

            case GET -> Root / "api" / "echo" :? DataQueryParam(data) => Ok(s"Here is the ${data}")

            case req @ POST -> Root / "api" / "echo" =>
                for
                    json <- req.as[Json]
                    _    <- IO.println(s"Received JSON: $json")
                    res  <- Ok(json)
                yield res
