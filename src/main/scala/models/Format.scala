package models

import com.github.plokhotnyuk.jsoniter_scala.core.*

enum Format:
    case PNG, SVG

object Format:
    def fromString(s: String): Format =
        s match
            case "PNG" => Format.PNG
            case "SVG" => Format.SVG
            case x     => throw new RuntimeException("Invalid Format: " + x)

    given JsonValueCodec[Format] with
        def decodeValue(in: JsonReader, default: Format): Format =
            fromString(in.readString(null))

        def encodeValue(x: Format, out: JsonWriter): Unit =
            out.writeVal(x.toString)

        def nullValue: Format = null
