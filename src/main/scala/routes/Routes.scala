package routes

import cats.effect.*
import cats.syntax.all.*
import org.http4s.*
import org.http4s.dsl.io.*
import models.QrModel
import service.QrCodeGen

import org.http4s.headers.`Content-Type`

import io.nayuki.qrcodegen.*

import javax.imageio.ImageIO

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*

import json.JsoniterCodecs.given
import models.Format
import java.io.ByteArrayOutputStream

object Routes:
    object DataQueryParam extends QueryParamDecoderMatcher[String]("data")

    private val apiRoutes: HttpRoutes[IO] =
        HttpRoutes.of[IO]:
            case GET -> Root / "api" / "health" =>
                Ok("Healthy")

            case req @ POST -> Root / "api" / "qr" =>
                for
                    qrModel <- req.as[QrModel]
                    _       <- IO.println(qrModel)
                    renderer = QrCodeGen(qrModel)
                    res <- qrModel.format match
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
                yield res

    private val indexRoute = HttpRoutes.of[IO]:
        case req @ GET -> Root => StaticFile.fromResource("/dist/index.html", Some(req)).getOrElseF(NotFound())

    val routes = apiRoutes <+> indexRoute
