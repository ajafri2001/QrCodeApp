package routes

import cats.effect.*
import cats.syntax.all.*
import models.Format
import models.QrModel
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.`Content-Type`
import service.QrCodeGen
import utils.JsoniterCodecs.given
import utils.Logger.given

import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import cats.data.Kleisli
import cats.data.OptionT

object Routes:
    object DataQueryParam extends QueryParamDecoderMatcher[String]("data")

    private val apiRoutes: HttpRoutes[IO] =

        HttpRoutes.of[IO]:
            case GET -> Root / "api" / "health" =>
                Ok("Healthy")

            case req @ POST -> Root / "api" / "qr" =>
                for
                    decoded <- req.attemptAs[QrModel].value
                    res     <- decoded match
                        case Right(qrModel) =>
                            logger.info(s"Decoded QR model: $qrModel") *> {
                                val renderer = QrCodeGen(qrModel)
                                qrModel.format match
                                    case Format.PNG =>
                                        for
                                            bytes <- IO.blocking:
                                                val baos = ByteArrayOutputStream()
                                                ImageIO.write(renderer.renderImage, "PNG", baos)
                                                baos.toByteArray
                                            res <- Ok(bytes).map(_.withContentType(`Content-Type`(MediaType.image.png)))
                                        yield res
                                    case Format.SVG =>
                                        Ok(renderer.renderSvg).map(_.withContentType(`Content-Type`(MediaType.image.`svg+xml`)))
                            }
                        case Left(failure) =>
                            logger.error(s"Failed to decode QR model: $failure") *> BadRequest("Invalid QR model")
                yield res

    private val indexRoute: HttpRoutes[IO] = HttpRoutes.of[IO]:
        case req @ GET -> Root => StaticFile.fromResource("/dist/index.html", Some(req)).getOrElseF(NotFound())

    val routes: HttpRoutes[IO] = apiRoutes <+> indexRoute
