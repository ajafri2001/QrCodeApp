import cats.effect.IO
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.implicits.*
import org.http4s.Method.*
import services.*
import db.*
import doobie.*
import models.*
import utils.JsoniterCodecs.given
import java.nio.file.{Files, Paths}
import routes.*

class RoutesTest extends CatsEffectSuite:

    def withApp[A](test: HttpApp[IO] => IO[A]): IO[A] =
        val dbPath = s"test-${System.nanoTime()}.db"

        val xa = Transactor.fromDriverManager[IO](
            "org.sqlite.JDBC",
            s"jdbc:sqlite:$dbPath",
            None
        )

        val userQueries  = UserQueries(xa)
        val qrQueries    = QrQueries(xa)
        val sessionStore = SessionStore()
        val userService  = UserService(userQueries, sessionStore)
        val qrService    = QrService(qrQueries)

        val httpApp = Routes(userService, qrService).routes.orNotFound

        Database.init(xa) *>
            test(httpApp).guarantee(
                IO.blocking(Files.deleteIfExists(Paths.get(dbPath)))
            )

    // ---------- TESTS ----------

    test("GET /api/me returns 403 when not authenticated") {
        withApp { httpApp =>
            val req = Request[IO](GET, uri"/api/me")

            httpApp.run(req).map { resp =>
                assertEquals(resp.status, Status.Forbidden)
            }
        }
    }

    test("signup + login + GET /api/me returns 200") {
        withApp { httpApp =>
            val email    = Email("test@test.com").get
            val password = Password("pass")

            val signup = UserSignup("test", email, password)
            val login  = UserLogin(email, password)

            for
                signupReq = Request[IO](POST, uri"/api/signup").withEntity(signup)
                _ <- httpApp.run(signupReq)

                loginReq = Request[IO](POST, uri"/api/login").withEntity(login)
                loginResp <- httpApp.run(loginReq)

                cookie = loginResp.cookies.find(_.name == "session_id").get

                meReq = Request[IO](GET, uri"/api/me")
                    .addCookie(RequestCookie(cookie.name, cookie.content))

                meResp <- httpApp.run(meReq)
            yield assertEquals(meResp.status, Status.Ok)
        }
    }

    test("GET /api/getHistory returns 403 without auth") {
        withApp { httpApp =>
            val req = Request[IO](GET, uri"/api/getHistory")

            httpApp.run(req).map { resp =>
                assertEquals(resp.status, Status.Forbidden)
            }
        }
    }

    test("DELETE /api/qr/{id} returns 403 without auth") {
        withApp { httpApp =>
            val randomId = java.util.UUID.randomUUID()

            val req = Request[IO](DELETE, uri"/api/qr" / randomId.toString)

            httpApp.run(req).map { resp =>
                assertEquals(resp.status, Status.Forbidden)
            }
        }
    }
