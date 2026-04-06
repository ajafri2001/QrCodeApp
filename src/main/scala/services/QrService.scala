package services

import cats.effect.*

import models.*
import javax.imageio.ImageIO
import utils.*
import org.http4s.headers.`Content-Type`
import java.io.ByteArrayOutputStream
import org.http4s.MediaType

final class QrService:

    def generate(model: QrModel): IO[(Array[Byte], `Content-Type`)] =
        val renderer = QrCodeGen(model)
        model.format match
            case Format.PNG =>
                IO.blocking:
                    val baos = ByteArrayOutputStream()
                    ImageIO.write(renderer.renderImage, "PNG", baos)
                    (baos.toByteArray, `Content-Type`(MediaType.image.png))

            case Format.SVG =>
                IO:
                    (renderer.renderSvg.getBytes, `Content-Type`(MediaType.image.`svg+xml`))
