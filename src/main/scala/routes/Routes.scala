package routes

import cats.data.Kleisli
import cats.data.OptionT
import cats.effect._
import cats.syntax.all._
import models._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._
import org.typelevel.ci.CIString
import services._
import utils.JsoniterCodecs.given

class Routes(userService: UserService, qrService: QrService) extends Http4sDsl[IO]:

    // Helper to extract authenticated user from request (via session/cookie/etc.)
    private def getUser(req: Request[IO]): IO[Option[User]] =
        userService.getUserFromRequest(req)

    // API endpoints
    private val apiRoutes: HttpRoutes[IO] =
        HttpRoutes.of[IO]:

            case req @ GET -> Root / "api" / "me" =>
                for
                    userOpt <- getUser(req)
                    resp    <- userOpt match
                        case Some(user) => Ok(user.name)
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

            case req @ POST -> Root / "api" / "logout" =>
                for
                    _   <- userService.logOutUser(req)
                    res <- Ok()
                yield res

            case req @ POST -> Root / "api" / "getQR" =>
                getUser(req).flatMap:
                    case Some(user) =>
                        for
                            model       <- req.as[QrModel]
                            (bytes, ct) <- qrService.generate(user, model)
                            res         <- Ok(bytes).map(_.withContentType(ct))
                        yield res

                    case None =>
                        Forbidden()

            case req @ GET -> Root / "api" / "getHistory" =>
                for
                    userOpt <- getUser(req)
                    resp    <- userOpt match
                        case Some(user) =>
                            qrService.getHistory(user).flatMap(Ok(_))

                        case None =>
                            Forbidden()
                yield resp

            case req @ GET -> Root / "api" / "download" / UUIDVar(id) =>
                for
                    userOpt <- getUser(req)
                    resp    <- userOpt match
                        case Some(user) =>
                            qrService.getQr(user, id).flatMap {
                                case Some((bytes, format)) =>

                                    // Set correct content type based on QR format
                                    val contentType = format match
                                        case Format.PNG => `Content-Type`(MediaType.image.png)
                                        case Format.SVG => `Content-Type`(MediaType.image.`svg+xml`)

                                    val filename = s"qr-$id.${format.toString.toLowerCase}"

                                    Ok(bytes).map(
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

            case req @ DELETE -> Root / "api" / "qr" / UUIDVar(id) =>
                for
                    userOpt <- getUser(req)
                    resp    <- userOpt match
                        case Some(user) =>
                            qrService.delete(id, user.id) *> NoContent()
                        case None =>
                            Forbidden()
                yield resp

    // SPA fallback (serves frontend)
    private val spaRoutes: HttpRoutes[IO] =
        HttpRoutes.of[IO]:
            case req @ GET -> _ =>
                StaticFile
                    .fromResource("/dist/index.html", Some(req))
                    .getOrElseF(NotFound())

    // Combined routes (API first, then SPA fallback)
    val routes: HttpRoutes[IO] =
        apiRoutes <+> spaRoutes
