package models

import com.github.plokhotnyuk.jsoniter_scala.core.*

// Output format for generated QR codes
enum Format:
    case PNG, SVG

object Format:

    // Parse string into Format (strict)
    def fromString(s: String): Format =
        s match
            case "PNG" => Format.PNG
            case "SVG" => Format.SVG
            case x     => throw new RuntimeException("Invalid Format: " + x)

    // JSON codec (jsoniter-scala)
    given JsonValueCodec[Format] with
        // Decode format from JSON string
        def decodeValue(in: JsonReader, default: Format): Format =
            fromString(in.readString(null))

        // Encode format as JSON string
        def encodeValue(x: Format, out: JsonWriter): Unit =
            out.writeVal(x.toString)

        // Fallback for null values
        def nullValue: Format = null
