package routes

import cats.effect.*
import cats.syntax.all.*
import models.*
import org.typelevel.ci.CIString
import utils.JsoniterCodecs.given
import org.http4s.headers.*
import org.http4s.*

import cats.data.Kleisli
import cats.data.OptionT
import services.*
import org.http4s.dsl.Http4sDsl

class Routes(userService: UserService, qrService: QrService) extends Http4sDsl[IO]:

    private def getUser(req: Request[IO]): IO[Option[User]] = userService.getUserFromRequest(req)

    private val apiRoutes: HttpRoutes[IO] =

        HttpRoutes.of[IO]:

            case req @ GET -> Root / "api" / "me" =>
                for
                    userOpt <- getUser(req)
                    resp    <- userOpt match
                        case Some(user) => Ok(user.name) // or return JSON later
                        case None       => Forbidden()
                yield resp

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

            case req @ POST -> Root / "api" / "getQR" =>
                for
                    userOpt <- getUser(req)
                    resp    <- userOpt match
                        case Some(user) =>
                            for
                                model       <- req.as[QrModel]
                                (bytes, ct) <- qrService.generate(user, model)
                                res         <- Ok(bytes).map(_.withContentType(ct))
                            yield res

                        case None =>
                            Forbidden()
                yield resp

            case req @ GET -> Root / "api" / "getHistory" =>
                for
                    userOpt <- getUser(req)
                    resp    <- userOpt match
                        case Some(user) =>
                            qrService.getHistory(user).flatMap(Ok(_))

                        case None =>
                            Forbidden()
                yield resp

            case req @ GET -> Root / "api" / "qr" / UUIDVar(id) =>
                for
                    userOpt <- getUser(req)
                    resp    <- userOpt match
                        case Some(user) =>
                            qrService.getQr(user, id).flatMap {
                                case Some((bytes, format)) =>

                                    val contentType = format match
                                        case Format.PNG => `Content-Type`(MediaType.image.png)
                                        case Format.SVG => `Content-Type`(MediaType.image.`svg+xml`)

                                    val filename = s"qr-$id.${format.toString.toLowerCase}"

                                    Ok(bytes)
                                        .map(
                                            _.withContentType(contentType)
                                                .putHeaders(
                                                    Header.Raw(
                                                        CIString("Content-Disposition"),
                                                        s"""attachment; filename="$filename""""
                                                    )
                                                )
                                        )

                                case None =>
                                    NotFound()
                            }

                        case None =>
                            Forbidden()
                yield resp

    private val spaRoutes: HttpRoutes[IO] =
        HttpRoutes.of[IO]:
            case req @ GET -> _ =>
                StaticFile
                    .fromResource("/dist/index.html", Some(req))
                    .getOrElseF(NotFound())

    val routes: HttpRoutes[IO] = apiRoutes <+> spaRoutes // Order is important here
