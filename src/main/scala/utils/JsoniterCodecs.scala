package utils

import cats.effect.IO
import com.github.plokhotnyuk.jsoniter_scala.core._
import org.http4s.DecodeResult
import org.http4s.MalformedMessageBodyFailure
import org.http4s.MediaType
import org.http4s._
import org.http4s.headers.`Content-Type`

object JsoniterCodecs:
    given [A: JsonValueCodec]: EntityEncoder[IO, A] =
        EntityEncoder.byteArrayEncoder
            .contramap[A](writeToArray(_))
            .withContentType(`Content-Type`(MediaType.application.json))

    given [A: JsonValueCodec]: EntityDecoder[IO, A] =
        EntityDecoder.decodeBy(MediaType.application.json): msg =>
            DecodeResult:
                msg.as[Array[Byte]].attempt.map:
                    case Right(bytes) if bytes.nonEmpty =>
                        Right(readFromArray[A](bytes))
                    case Right(_) =>
                        Left(MalformedMessageBodyFailure("Empty JSON body"))
                    case Left(e) =>
                        Left(MalformedMessageBodyFailure("Invalid JSON", Some(e)))
