package models

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*

final case class QrModel(
    url: String,
    ecc: ErrorCorrection,
    format: Format,
    scale: Option[Int],
    border: Int,
    lightColor: String,
    darkColor: String
)

object QrModel:
    given JsonValueCodec[QrModel] = JsonCodecMaker.make
