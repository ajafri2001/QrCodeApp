package models

import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._

import java.util.UUID
import scala.util.matching.Regex

// Core user entity stored in DB
case class User(id: UUID, name: String, email: Email, password: Password)
object User:
    given JsonValueCodec[User] = JsonCodecMaker.make

// Signup request model (used for registration)
case class UserSignup(name: String, email: Email, password: Password):
    def toUser: User = User(UUID.randomUUID(), this.name, this.email, this.password)

object UserSignup:
    given JsonValueCodec[UserSignup] = JsonCodecMaker.make

// Login request model
case class UserLogin(email: Email, password: Password)
object UserLogin:
    given JsonValueCodec[UserLogin] = JsonCodecMaker.make

// Email value type with validation
opaque type Email = String
object Email:

    given JsonValueCodec[Email] = JsonCodecMaker.make

    // Basic RFC-style email validation regex
    private val emailRegex: Regex =
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".r

    // Smart constructor (validates format)
    def apply(value: String): Option[Email] =
        if emailRegex.matches(value) then Some(value)
        else None

    extension (e: Email)
        def value: String = e

// Password wrapper (no hashing logic here; just type safety boundary)
opaque type Password = String
object Password:

    given JsonValueCodec[Password] = JsonCodecMaker.make

    // Unsafe constructor (hashing handled in service layer)
    def apply(value: String): Password = value

    extension (p: Password)
        def value: String = p
