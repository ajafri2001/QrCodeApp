package services

import cats.effect.*
import cats.effect.std.UUIDGen
import models.User
import scala.collection.concurrent.TrieMap

class SessionStore:
    private val sessions = TrieMap.empty[String, User]

    def create(user: User): IO[String] =
        UUIDGen[IO].randomUUID.map: id =>
            val sessionId = id.toString
            sessions.put(sessionId, user)
            sessionId

    def get(sessionId: String): IO[Option[User]] =
        IO(sessions.get(sessionId))

    def delete(sessionId: String): IO[Unit] =
        IO(sessions.remove(sessionId)).void
