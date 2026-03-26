import cats.effect.*
import cats.syntax.semigroupk.*
import com.comcast.ip4s.*
import org.http4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.staticcontent.*
import routes.Routes
import utils.Logger.given

object Main extends IOApp:

    def run(args: List[String]): IO[ExitCode] =
        for
            staticRoute <- resourceServiceBuilder[IO]("/dist").toRoutes
            _           <- EmberServerBuilder
                .default[IO]
                .withHost(host"0.0.0.0")
                .withPort(port"8080")
                .withHttpApp((Routes.routes <+> staticRoute).orNotFound)
                .build
                .useForever
        yield ExitCode.Success
