package models

import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._

// Request model for QR generation API
final case class QrModel(
    url: String,          // target URL encoded into QR
    ecc: ErrorCorrection, // error correction level
    format: Format,       // output format (PNG / SVG)
    scale: Option[Int],   // optional pixel scale for raster output
    border: Int,          // quiet zone size around QR
    lightColor: String,   // background color (hex)
    darkColor: String     // foreground color (hex)
)

object QrModel:

    // Auto-derived JSON codec
    given JsonValueCodec[QrModel] = JsonCodecMaker.make
