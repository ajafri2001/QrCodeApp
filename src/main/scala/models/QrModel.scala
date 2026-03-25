package models

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.*
import io.nayuki.qrcodegen.QrCode.Ecc

enum Format:
    case PNG, SVG

object Format:
    given Decoder[Format] = deriveDecoder
    given Encoder[Format] = deriveEncoder

enum ErrorCorrection:
    case Low, Medium, Quartile, High

object ErrorCorrection:
    given Decoder[ErrorCorrection] = deriveDecoder
    given Encoder[ErrorCorrection] = deriveEncoder


final case class QrModel(
    url: String,
    errorCorrection: ErrorCorrection,
    outputFormat: Format,
    scale: Option[Int],
    border: Int,
    lightColor: Int,
    darkColor: Int
)

object QrModel:
    given Decoder[QrModel] = deriveDecoder
    given Encoder[QrModel] = deriveEncoder
