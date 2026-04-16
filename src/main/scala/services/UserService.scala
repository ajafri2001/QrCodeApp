package services

import cats.effect._
import com.github.t3hnar.bcrypt._
import db.UserQueries
import models._
import org.http4s.Request

class UserService(userQueries: UserQueries, sessionStore: SessionStore):

    // User registration with email uniqueness check + password hashing
    def registerUser(signup: UserSignup): IO[Unit] =
        for
            exists <- userQueries.existsByEmail(signup.email)
            _      <- if exists then IO.raiseError(Exception("Email already in use")) else IO.unit
            hashed <- IO.pure(signup.password.value.boundedBcrypt)
            user   <- IO.pure(signup.toUser.copy(password = Password(hashed)))
            _      <- userQueries.insert(user)
        yield ()

    // Login validation + session creation
    def loginUser(login: UserLogin): IO[Either[String, String]] =
        for
            userOpt <- userQueries.findByEmail(login.email)
            result  <- userOpt match
                case None       => IO.pure(Left("Invalid email or password"))
                case Some(user) =>
                    if login.password.value.isBcryptedBounded(user.password.value) then
                        sessionStore.create(user).map(Right(_))
                    else IO.pure(Left("Invalid email or password"))
        yield result

    // Extract authenticated user from request cookie (session lookup)
    def getUserFromRequest(req: Request[IO]): IO[Option[User]] =
        req.cookies.find(_.name == "session_id") match
            case Some(cookie) => sessionStore.get(cookie.content)
            case None         => IO.pure(None)

    // Logout by removing session from store
    def logOutUser(req: Request[IO]): IO[Unit] =
        req.cookies.find(_.name == "session_id") match
            case Some(cookie) => sessionStore.delete(cookie.content)
            case None         => IO.unit
