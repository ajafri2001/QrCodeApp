package models

import com.github.plokhotnyuk.jsoniter_scala.macros.*
import com.github.plokhotnyuk.jsoniter_scala.core.*

enum Format:
    case PNG, SVG

object Format:
    given JsonValueCodec[Format] with
        def decodeValue(in: JsonReader, default: Format): Format =
            in.readString(null) match
                case "PNG" => Format.PNG
                case "SVG" => Format.SVG
                case x     => throw new RuntimeException("Invalid Format: " + x)

        def encodeValue(x: Format, out: JsonWriter): Unit =
            out.writeVal(x.toString)

        def nullValue: Format = null
