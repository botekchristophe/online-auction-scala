package com.example.auction.user.impl

import akka.Done
import com.example.auction.utils.JsonFormats.singletonFormat
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import play.api.libs.json.{Format, Json}


sealed trait UserCommand

case class CreateUser(username: String, email: String) extends UserCommand with ReplyType[Done]

object CreateUser {
  implicit val format: Format[CreateUser] = Json.format[CreateUser]
}

case class UpdateUser(username: Option[String] = None, email: Option[String] = None) extends UserCommand with ReplyType[User]

object UpdateUser {
  implicit val format: Format[UpdateUser] = Json.format[UpdateUser]
}

case object DeleteUser extends UserCommand with ReplyType[Done] {
  implicit val format: Format[DeleteUser.type] = singletonFormat(DeleteUser)
}

case object GetUser extends UserCommand with ReplyType[Option[User]] {
  implicit val format: Format[GetUser.type] = singletonFormat(GetUser)
}
