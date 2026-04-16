import cats.effect._
import cats.syntax.semigroupk._
import com.comcast.ip4s._
import db._
import doobie.util.log.LogHandler
import doobie.util.transactor.Transactor
import org.http4s._
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.staticcontent._
import routes.Routes
import services.QrService
import services.SessionStore
import services.UserService
import utils.Logger.given

object Main extends IOApp:

    // Doobie SQL logging
    val logHandler: LogHandler[IO] = LogHandler.jdkLogHandler[IO]

    // SQLite transactor
    val xa: Transactor[IO]{type A = Unit} = Transactor.fromDriverManager[IO](
        "org.sqlite.JDBC",
        "jdbc:sqlite:sqlite.db",
        Some(logHandler)
    )

    def run(args: List[String]): IO[ExitCode] =
        // DB access layer
        val userQueries = UserQueries(xa)
        val qrQueries   = QrQueries(xa)

        // Business services
        val userService = UserService(userQueries, SessionStore())
        val qrService   = QrService(qrQueries)

        // HTTP routes
        val routes = Routes(userService, qrService)

        for
            // Ensure schema exists
            _ <- Database.init(xa)

            // Static frontend (e.g. /dist build output)
            staticRoutes <- resourceServiceBuilder[IO]("/dist").toRoutes

            // Start HTTP server
            _ <- EmberServerBuilder
                .default[IO]
                .withHost(host"0.0.0.0")
                .withPort(port"3000")
                .withHttpApp((staticRoutes <+> routes.routes).orNotFound)
                .build
                .useForever
        yield ExitCode.Success
