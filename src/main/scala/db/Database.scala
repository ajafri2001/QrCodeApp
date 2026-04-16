package db

import cats.effect.IO
import cats.implicits._
import doobie._
import doobie.implicits._

object Database:

    // Initializes database schema (idempotent)
    def init(xa: Transactor[IO]): IO[Unit] =

        // Users table: core authentication data
        val createUsers =
            sql"""
                  CREATE TABLE IF NOT EXISTS users (
                    id            TEXT PRIMARY KEY,
                    name          TEXT NOT NULL,
                    email         TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL
                  )
               """.update.run

        // QR records table: stores generated QR codes per user
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

        // Execute schema creation in sequence
        (createUsers >> createQrRecords).transact(xa).void
