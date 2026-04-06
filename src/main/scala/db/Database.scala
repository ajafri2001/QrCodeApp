import cats.effect.IO
import doobie.*
import doobie.implicits.*

object Database:

    def init(xa: Transactor[IO]): IO[Unit] =
        val createUsers =
            sql"""
                  CREATE TABLE IF NOT EXISTS users (
                    id            TEXT PRIMARY KEY,
                    name          TEXT NOT NULL,
                    email         TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL
                  )
               """.update.run

        createUsers.transact(xa).void
