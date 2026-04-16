package models

import com.github.plokhotnyuk.jsoniter_scala.core._
import io.nayuki.qrcodegen.QrCode.Ecc

// QR error correction level
enum ErrorCorrection:
    case Low, Medium, Quartile, High

    // Convert domain model to QR library ECC enum
    def toJavaEcc: Ecc = this match
        case Low      => Ecc.LOW
        case Medium   => Ecc.MEDIUM
        case Quartile => Ecc.QUARTILE
        case High     => Ecc.HIGH

object ErrorCorrection:

    // JSON codec for serialization/deserialization
    given JsonValueCodec[ErrorCorrection] with

        // Decode ECC from JSON string
        def decodeValue(in: JsonReader, default: ErrorCorrection): ErrorCorrection =
            in.readString(null) match
                case "Low"      => ErrorCorrection.Low
                case "Medium"   => ErrorCorrection.Medium
                case "Quartile" => ErrorCorrection.Quartile
                case "High"     => ErrorCorrection.High
                case x          => throw new RuntimeException("Invalid ECC: " + x)

        // Encode ECC as JSON string
        def encodeValue(x: ErrorCorrection, out: JsonWriter): Unit =
            out.writeVal(x.toString)

        // Fallback value for null JSON
        def nullValue: ErrorCorrection = null
