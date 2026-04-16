import cats.effect.*
import munit.CatsEffectSuite
import db.*
import models.*
import doobie.*
import java.nio.file.{Files, Paths}
import services.*

class UserServiceTest extends CatsEffectSuite:

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

    // ---------- TESTS ----------

    test("registerUser inserts a new user") {
        withDb { xa =>
            val userQueries = UserQueries(xa)
            val service     = UserService(userQueries, SessionStore())

            val email    = Email("test@test.com").get
            val password = Password("pass")
            val signup   = UserSignup("test", email, password)

            for
                _       <- service.registerUser(signup)
                userOpt <- userQueries.findByEmail(email)
            yield assert(userOpt.isDefined)
        }
    }

    test("registerUser fails if email already exists") {
        withDb { xa =>
            val userQueries = UserQueries(xa)
            val service     = UserService(userQueries, SessionStore())

            val email    = Email("test@test.com").get
            val password = Password("pass")
            val signup   = UserSignup("test", email, password)

            for
                _      <- service.registerUser(signup)
                result <- service.registerUser(signup).attempt
            yield assert(result.isLeft)
        }
    }

    test("loginUser returns session id on correct credentials") {
        withDb { xa =>
            val userQueries = UserQueries(xa)
            val service     = UserService(userQueries, SessionStore())

            val email    = Email("test@test.com").get
            val password = Password("pass")
            val signup   = UserSignup("test", email, password)

            for
                _      <- service.registerUser(signup)
                result <- service.loginUser(UserLogin(email, password))
            yield assert(result.isRight)
        }
    }

    test("loginUser fails with wrong password") {
        withDb { xa =>
            val userQueries = UserQueries(xa)
            val service     = UserService(userQueries, SessionStore())

            val email = Email("test@test.com").get

            val signup     = UserSignup("test", email, Password("correct"))
            val wrongLogin = UserLogin(email, Password("wrong"))

            for
                _      <- service.registerUser(signup)
                result <- service.loginUser(wrongLogin)
            yield assert(result.isLeft)
        }
    }

    test("getUserFromRequest returns user when session exists") {
        withDb { xa =>
            val userQueries = UserQueries(xa)
            val service     = UserService(userQueries, SessionStore())

            val email    = Email("test@test.com").get
            val password = Password("pass")
            val signup   = UserSignup("test", email, password)

            for
                _        <- service.registerUser(signup)
                loginRes <- service.loginUser(UserLogin(email, password))

                sessionId = loginRes.toOption.get

                req = org.http4s.Request[IO]()
                    .addCookie(org.http4s.RequestCookie("session_id", sessionId))

                userOpt <- service.getUserFromRequest(req)
            yield assert(userOpt.isDefined)
        }
    }

    test("logOutUser removes session") {
        withDb { xa =>
            val userQueries = UserQueries(xa)
            val service     = UserService(userQueries, SessionStore())

            val email    = Email("test@test.com").get
            val password = Password("pass")
            val signup   = UserSignup("test", email, password)

            for
                _        <- service.registerUser(signup)
                loginRes <- service.loginUser(UserLogin(email, password))

                sessionId = loginRes.toOption.get

                req = org.http4s.Request[IO]()
                    .addCookie(org.http4s.RequestCookie("session_id", sessionId))

                _ <- service.logOutUser(req)

                userOpt <- service.getUserFromRequest(req)
            yield assertEquals(userOpt, None)
        }
    }
