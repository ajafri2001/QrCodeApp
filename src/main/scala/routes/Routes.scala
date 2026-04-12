package routes

import cats.effect.*
import cats.syntax.all.*
import models.*
import org.http4s.*
import utils.JsoniterCodecs.given

import cats.data.Kleisli
import cats.data.OptionT
import services.*
import org.http4s.dsl.Http4sDsl

class Routes(userService: UserService, qrService: QrService) extends Http4sDsl[IO]:

    private val apiRoutes: HttpRoutes[IO] =

        HttpRoutes.of[IO]:

            case req @ POST -> Root / "api" / "login" =>
                for
                    login  <- req.as[UserLogin]
                    result <- userService.loginUser(login)
                    resp   <- result match
                        case Right(sessionId) =>
                            val cookie = ResponseCookie(
                                name = "session_id",
                                content = sessionId,
                                httpOnly = true,
                                path = Some("/")
                            )
                            Ok().map(_.addCookie(cookie))

                        case Left(err) =>
                            Forbidden(err)
                yield resp

            case req @ POST -> Root / "api" / "signup" =>
                for
                    signup <- req.as[UserSignup]
                    _      <- userService.registerUser(signup)
                    resp   <- Ok()
                yield resp

            case req @ POST -> Root / "api" / "qr" =>
                for
                    model                <- req.as[QrModel]
                    (bytes, contentType) <- qrService.generate(model)
                    res                  <- Ok(bytes).map(_.withContentType(contentType))
                yield res

    private val protectedRoutes = ???

    private val spaRoutes: HttpRoutes[IO] =
        HttpRoutes.of[IO]:
            case req @ GET -> _ =>
                StaticFile
                    .fromResource("/dist/index.html", Some(req))
                    .getOrElseF(NotFound())

    val routes: HttpRoutes[IO] = apiRoutes <+> spaRoutes // Order is important here
