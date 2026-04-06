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
import db.UserQueries
import services.UserService
import services.QrService

object Main extends IOApp:

    val logHandler = LogHandler.jdkLogHandler[IO]

    val xa = Transactor.fromDriverManager[IO](
        "org.sqlite.JDBC",
        "jdbc:sqlite:sqlite.db",
        Some(logHandler)
    )

    def run(args: List[String]): IO[ExitCode] =
        for
            _ <- Database.init(xa)
            userQueries = UserQueries(xa)
            userService = UserService(userQueries)
            qrService   = QrService()
            routes      = Routes(userService, qrService)
            staticRoutes <- resourceServiceBuilder[IO]("/dist").toRoutes
            _            <- EmberServerBuilder
                .default[IO]
                .withHost(host"0.0.0.0")
                .withPort(port"3000")
                .withHttpApp((staticRoutes <+> routes.routes).orNotFound) // order is important
                .build
                .useForever
        yield ExitCode.Success
