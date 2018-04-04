package com.example.auction.user.api

import java.util.UUID

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import play.api.libs.json.{Format, Json}

trait UserService extends Service {
  def getUsers: ServiceCall[NotUsed, Seq[User]]
  def getUser(id: UUID): ServiceCall[NotUsed, User]
  def createUser: ServiceCall[UserCreationRequest, User]
  def updateUser(id: UUID): ServiceCall[UserUpdateRequest, User]
  def deleteUser(id: UUID): ServiceCall[NotUsed, NotUsed]

  override final def descriptor: Descriptor = {
    import Service._
    named("user").withCalls(
      pathCall("/api/user", getUsers _),
      pathCall("/api/user/:id", getUser _),
      pathCall("/api/user", createUser _),
      pathCall("/api/user/:id", updateUser _),
      pathCall("/api/user/:id", deleteUser _)
    )
  }
}

case class User(id: UUID, username: String, email: String)

object User {
  implicit val format: Format[User] = Json.format[User]
}

case class UserCreationRequest(username: String,
                               email: String = "name@example.com",
                               password: String = "xxxxx")

object UserCreationRequest {
  implicit val format: Format[UserCreationRequest] = Json.format[UserCreationRequest]
}

case class UserUpdateRequest(username: Option[String] = None, email: Option[String] = None)

object UserUpdateRequest {
  implicit val format: Format[UserUpdateRequest] = Json.format[UserUpdateRequest]
}