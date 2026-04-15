import cats.effect.IO
import cats.implicits.*
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

        val createQrRecords =
            sql"""
                  CREATE TABLE IF NOT EXISTS qr_records (
                    id           TEXT PRIMARY KEY,
                    user_id      TEXT NOT NULL,
                    original_url TEXT NOT NULL,
                    mime_type    TEXT NOT NULL,
                    data         BLOB NOT NULL,
                    created_at   TIMESTAMP NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                  )
               """.update.run

        (createUsers >> createQrRecords).transact(xa).void
