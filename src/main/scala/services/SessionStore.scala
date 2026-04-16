package services

import cats.effect.*
import cats.effect.std.UUIDGen
import models.User
import scala.collection.concurrent.TrieMap

class SessionStore:

    // In-memory session storage (non-persistent, process-local)
    private val sessions = TrieMap.empty[String, User]

    // Create a new session ID and associate it with a user
    def create(user: User): IO[String] =
        UUIDGen[IO].randomUUID.map: id =>
            val sessionId = id.toString
            sessions.put(sessionId, user)
            sessionId

    // Retrieve user from session ID if it exists
    def get(sessionId: String): IO[Option[User]] =
        IO(sessions.get(sessionId))

    // Remove session (logout)
    def delete(sessionId: String): IO[Unit] =
        IO(sessions.remove(sessionId)).void
