package models

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import io.nayuki.qrcodegen.QrCode.Ecc

enum ErrorCorrection:
    case Low, Medium, Quartile, High

    def toJavaEcc: Ecc = this match
        case Low      => Ecc.LOW
        case Medium   => Ecc.MEDIUM
        case Quartile => Ecc.QUARTILE
        case High     => Ecc.HIGH

object ErrorCorrection:
    given JsonValueCodec[ErrorCorrection] with
        def decodeValue(in: JsonReader, default: ErrorCorrection): ErrorCorrection =
            in.readString(null) match
                case "Low"      => ErrorCorrection.Low
                case "Medium"   => ErrorCorrection.Medium
                case "Quartile" => ErrorCorrection.Quartile
                case "High"     => ErrorCorrection.High
                case x          => throw new RuntimeException("Invalid ECC: " + x)

        def encodeValue(x: ErrorCorrection, out: JsonWriter): Unit =
            out.writeVal(x.toString)

        def nullValue: ErrorCorrection = null
