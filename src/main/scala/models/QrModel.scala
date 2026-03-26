package models

import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._

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
