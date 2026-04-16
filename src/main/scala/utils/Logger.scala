package utils

import cats.effect.IO
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Logger:

    // Log4cats SLF4J-backed logger factory for IO
    given LoggerFactory[IO] = Slf4jFactory.create[IO]

    // Default implicit logger instance for application-wide use
    given logger: Logger[IO] = Slf4jLogger.getLogger[IO]
