package services

import cats.effect.*

import models.*
import javax.imageio.ImageIO
import utils.*
import org.http4s.headers.`Content-Type`
import java.io.ByteArrayOutputStream
import org.http4s.MediaType
import db.QrQueries

import java.util.UUID
import java.time.Instant

final class QrService(qrQueries: QrQueries):

    def generate(user: User, model: QrModel): IO[(Array[Byte], `Content-Type`)] =
        val renderer = QrCodeGen(model)

        val generateBytes: IO[(Array[Byte], `Content-Type`)] =
            model.format match
                case Format.PNG =>
                    IO.blocking:
                        val baos = ByteArrayOutputStream()
                        ImageIO.write(renderer.renderImage, "PNG", baos)
                        (baos.toByteArray, `Content-Type`(MediaType.image.png))

                case Format.SVG =>
                    IO:
                        (renderer.renderSvg.getBytes, `Content-Type`(MediaType.image.`svg+xml`))

        for
            (bytes, contentType) <- generateBytes

            record = QrRecord(
                id = UUID.randomUUID(),
                userId = user.id,
                originalUrl = model.url,
                mimeType = model.format,
                image = bytes,
                createdAt = Instant.now()
            )

            _ <- qrQueries.insert(record)
        yield (bytes, contentType)

    def getHistory(user: User): IO[List[QrRecordView]] =
        for
            records <- qrQueries.findByUser(user.id)
        yield records.map: r =>
            QrRecordView(
                id = r.id,
                originalUrl = r.originalUrl,
                mimeType = r.mimeType,
                createdAt = r.createdAt
            )

    def getQr(user: User, id: UUID): IO[Option[(Array[Byte], Format)]] =
        qrQueries.findById(id, user.id).map(_.map(r => (r.image, r.mimeType)))

    def delete(rowId: UUID, userId: UUID): IO[Unit] =
        qrQueries.delete(rowId, userId)
