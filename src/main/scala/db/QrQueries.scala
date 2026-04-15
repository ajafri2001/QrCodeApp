package db

import doobie.*
import doobie.implicits.*
import doobie.util.transactor.Transactor
import cats.effect.*
import java.util.UUID
import java.time.Instant
import models.{Format, QrRecord}

final class QrQueries(xa: Transactor[IO]):

    given Meta[UUID] =
        Meta[String].timap(UUID.fromString)(_.toString)

    given Meta[Instant] =
        Meta[String].timap(Instant.parse)(_.toString)

    given Meta[Format] =
        Meta[String].timap(Format.fromString)(_.toString)

    def insert(record: QrRecord): IO[Unit] =
        sql"""
      INSERT INTO qr_records (
        id,
        user_id,
        original_url,
        mime_type,
        data,
        created_at
      )
      VALUES (
        ${record.id},
        ${record.userId},
        ${record.originalUrl},
        ${record.mimeType},
        ${record.image},
        ${record.createdAt}
      )
    """.update.run
            .transact(xa)
            .void

    def findByUser(userId: UUID): IO[List[QrRecord]] =
        sql"""
      SELECT
        id,
        user_id,
        original_url,
        mime_type,
        data,
        created_at
      FROM qr_records
      WHERE user_id = $userId
      ORDER BY created_at DESC
    """
            .query[QrRecord]
            .to[List]
            .transact(xa)

    def findById(id: UUID, userId: UUID): IO[Option[QrRecord]] =
        sql"""
      SELECT
        id,
        user_id,
        original_url,
        mime_type,
        data,
        created_at
      FROM qr_records
      WHERE id = $id AND user_id = $userId
    """
            .query[QrRecord]
            .option
            .transact(xa)

    def delete(id: UUID, userId: UUID): IO[Unit] =
        sql"""
      DELETE FROM qr_records
      WHERE id = $id AND user_id = $userId
    """
            .update.run
            .transact(xa)
            .void
