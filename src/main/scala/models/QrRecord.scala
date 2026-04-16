package models

import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._

import java.time.{Instant => Date}
import java.util.UUID

// Persisted QR record (stored in DB, includes binary image)
final case class QrRecord(
    id: UUID,
    userId: UUID,
    originalUrl: String,
    mimeType: Format,
    image: Array[Byte],
    createdAt: Date
)

object QrRecord:

    given JsonValueCodec[QrRecord] = JsonCodecMaker.make

// Lightweight view model for API responses (no binary payload)
final case class QrRecordView(
    id: UUID,
    originalUrl: String,
    mimeType: Format,
    createdAt: Date
)

object QrRecordView:

    given JsonValueCodec[QrRecordView] = JsonCodecMaker.make

    // List view codec for history endpoints
    given JsonValueCodec[List[QrRecordView]] = JsonCodecMaker.make
