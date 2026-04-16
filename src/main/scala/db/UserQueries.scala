package db

import doobie.*
import doobie.implicits.*
import doobie.util.transactor.Transactor
import cats.effect.*
import models.{Email, Password, User}
import java.util.UUID

class UserQueries(xa: Transactor[IO]):

    // Doobie mapping for UUID <-> String
    given Meta[UUID] = Meta[String].timap(UUID.fromString)(_.toString)

    // Doobie mapping for Email (validated domain type)
    given Meta[Email] =
        Meta[String].timap(s =>
            Email(s).getOrElse(
                throw new IllegalArgumentException(s"Invalid email in DB: $s")
            )
        )(_.value)

    // Doobie mapping for Password wrapper type
    given Meta[Password] = Meta[String].timap(Password(_))(_.value)

    // Insert a new user into DB
    def insert(user: User): IO[Unit] =
        sql"""
          INSERT INTO users (id, name, email, password_hash)
          VALUES (${user.id}, ${user.name}, ${user.email}, ${user.password})
        """.update.run
            .transact(xa)
            .void

    // Fetch user by ID
    def findById(id: UUID): IO[Option[User]] =
        sql"""
          SELECT id, name, email, password_hash
          FROM users
          WHERE id = $id
        """.query[User]
            .option
            .transact(xa)

    // Fetch user by email (login lookup)
    def findByEmail(email: Email): IO[Option[User]] =
        sql"""
          SELECT id, name, email, password_hash
          FROM users
          WHERE email = $email
        """.query[User]
            .option
            .transact(xa)

    // Check whether email already exists (used for registration)
    def existsByEmail(email: Email): IO[Boolean] =
        sql"""
          SELECT EXISTS (
            SELECT 1 FROM users WHERE email = $email
          )
        """.query[Boolean]
            .unique
            .transact(xa)
