import cats.effect.*
import com.comcast.ip4s.*
import org.http4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import routes.Routes

object Main extends IOApp:

    given LoggerFactory[IO] = Slf4jFactory.create[IO]

    def run(args: List[String]): IO[ExitCode] =
        EmberServerBuilder
            .default[IO]
            .withHost(host"0.0.0.0")
            .withPort(port"8080")
            .withHttpApp(Routes.routes.orNotFound)
            .build
            .useForever
