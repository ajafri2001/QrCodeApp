package models

import java.util.UUID
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*

import scala.util.matching.Regex

case class User(id: UUID, name: String, email: Email, password: Password)
object User:
    given JsonValueCodec[User] = JsonCodecMaker.make

case class UserSignup(name: String, email: Email, password: Password):
    def toUser: User = User(UUID.randomUUID(), this.name, this.email, this.password)

object UserSignup:
    given JsonValueCodec[UserSignup] = JsonCodecMaker.make

case class UserLogin(email: Email, password: Password)
object UserLogin:
    given JsonValueCodec[UserLogin] = JsonCodecMaker.make

opaque type Email = String
object Email:
    given JsonValueCodec[Email] = JsonCodecMaker.make

    private val emailRegex: Regex =
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".r

    def apply(value: String): Option[Email] =
        if emailRegex.matches(value) then Some(value)
        else None

    extension (e: Email)
        def value: String = e

opaque type Password = String
object Password:
    given JsonValueCodec[Password] = JsonCodecMaker.make

    def apply(value: String): Password = value

    extension (p: Password)
        def value: String = p
