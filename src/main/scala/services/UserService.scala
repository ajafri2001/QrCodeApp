package services

import db.UserQueries
import models.*
import cats.effect.*
import com.github.t3hnar.bcrypt.*

class UserService(userQueries: UserQueries, sessionStore: SessionStore):

    def registerUser(signup: UserSignup): IO[Unit] =
        for
            exists <- userQueries.existsByEmail(signup.email)
            _      <- if exists then IO.raiseError(Exception("Email already in use"))
            else IO.unit
            hashed = signup.password.value.boundedBcrypt
            user   = signup.toUser.copy(password = Password(hashed))
            _ <- userQueries.insert(user)
        yield ()

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
