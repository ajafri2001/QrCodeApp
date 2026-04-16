import cats.effect.*
import munit.CatsEffectSuite
import db.*
import models.*
import doobie.*
import java.util.UUID
import java.nio.file.{Files, Paths}
import services.*

class QrServiceTest extends CatsEffectSuite:

    def withDb[A](test: Transactor[IO] => IO[A]): IO[A] =
        val dbPath = s"test-${System.nanoTime()}.db"

        val xa = Transactor.fromDriverManager[IO](
            "org.sqlite.JDBC",
            s"jdbc:sqlite:$dbPath",
            None
        )

        Database.init(xa) *>
            test(xa).guarantee(
                IO.blocking(Files.deleteIfExists(Paths.get(dbPath)))
            )

    // ---------- helpers ----------

    def sampleUser =
        User(UUID.randomUUID(), "test", Email("test@test.com").get, Password("pass"))

    def sampleModel(format: Format) =
        QrModel(
            url = "https://example.com",
            ecc = ErrorCorrection.Medium,
            format = format,
            scale = Some(5),
            border = 2,
            lightColor = "#ffffff",
            darkColor = "#000000"
        )

    // ---------- TESTS ----------

    test("generate stores QR and returns PNG bytes") {
        withDb { xa =>
            val qrQueries = QrQueries(xa)
            val service   = QrService(qrQueries)
            val user      = sampleUser

            for
                (bytes, ct) <- service.generate(user, sampleModel(Format.PNG))
                records     <- qrQueries.findByUser(user.id)
            yield
                assert(bytes.nonEmpty)
                assertEquals(ct.mediaType, org.http4s.MediaType.image.png)
                assertEquals(records.length, 1)
        }
    }

    test("generate stores QR and returns SVG bytes") {
        withDb { xa =>
            val qrQueries = QrQueries(xa)
            val service   = QrService(qrQueries)
            val user      = sampleUser

            for
                (bytes, ct) <- service.generate(user, sampleModel(Format.SVG))
                records     <- qrQueries.findByUser(user.id)
            yield
                assert(bytes.nonEmpty)
                assertEquals(ct.mediaType, org.http4s.MediaType.image.`svg+xml`)
                assertEquals(records.length, 1)
        }
    }

    test("getHistory returns stored records") {
        withDb { xa =>
            val qrQueries = QrQueries(xa)
            val service   = QrService(qrQueries)
            val user      = sampleUser

            for
                _       <- service.generate(user, sampleModel(Format.PNG))
                _       <- service.generate(user, sampleModel(Format.SVG))
                history <- service.getHistory(user)
            yield assertEquals(history.length, 2)
        }
    }

    test("getQr returns stored QR bytes") {
        withDb { xa =>
            val qrQueries = QrQueries(xa)
            val service   = QrService(qrQueries)
            val user      = sampleUser

            for
                _       <- service.generate(user, sampleModel(Format.PNG))
                records <- qrQueries.findByUser(user.id)

                id = records.head.id

                result <- service.getQr(user, id)
            yield
                assert(result.isDefined)
                val (bytes, format) = result.get
                assert(bytes.nonEmpty)
                assertEquals(format, Format.PNG)
        }
    }

    test("delete removes QR record") {
        withDb { xa =>
            val qrQueries = QrQueries(xa)
            val service   = QrService(qrQueries)
            val user      = sampleUser

            for
                _       <- service.generate(user, sampleModel(Format.PNG))
                records <- qrQueries.findByUser(user.id)

                id = records.head.id

                _ <- service.delete(id, user.id)

                remaining <- qrQueries.findByUser(user.id)
            yield assertEquals(remaining.length, 0)
        }
    }
