import cats.effect.*
import cats.syntax.semigroupk.*
import com.comcast.ip4s.*
import org.http4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.staticcontent.*
import routes.Routes
import utils.Logger.given
import doobie.util.transactor.Transactor
import doobie.util.log.LogHandler
import db.*
import services.UserService
import services.QrService
import services.SessionStore

object Main extends IOApp:

    // Doobie SQL logging (JDK backend)
    val logHandler = LogHandler.jdkLogHandler[IO]

    // SQLite transactor (file-based DB)
    val xa = Transactor.fromDriverManager[IO](
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
