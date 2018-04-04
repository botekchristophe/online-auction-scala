package com.example.auction.user.impl

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import play.api.libs.json.{Format, Json}

class UserEntity extends PersistentEntity {
  override type Command = UserCommand
  override type Event = UserEvent
  override type State = Option[User]

  type OnCommandHandler[M] = PartialFunction[(Command, CommandContext[M], State), Persist]
  type ReadOnlyHandler[M] = PartialFunction[(Command, ReadOnlyCommandContext[M], State), Unit]

  override def initialState: Option[User] = None

  override def behavior: Behavior = {
    case Some(_) =>
      Actions()
        .onReadOnlyCommand[GetUser.type, Option[User]] { getUserHandler }
        .onReadOnlyCommand[CreateUser, Done] { case (_, ctx, _) => ctx.invalidCommand("User already exists") }
        .onCommand[UpdateUser, User] { updateUserHandler }
        .onCommand[DeleteUser.type, Done] { deleteUserHandler }
        .onEvent { onUserEvent }
    case None =>
      Actions()
        .onReadOnlyCommand[GetUser.type, Option[User]] { getUserHandler }
        .onCommand[CreateUser, Done] { createUserHandler }
        .onReadOnlyCommand[UpdateUser, User] { case (_, ctx, _) => ctx.invalidCommand("User does not exist") }
        .onCommand[DeleteUser.type, Done] { deleteUserHandler }
        .onEvent { onUserEvent }
  }

  val getUserHandler: ReadOnlyHandler[State] = {
    case (GetUser, _, state) => state
  }

  val createUserHandler: OnCommandHandler[Done] = {
    case (CreateUser(username, email), ctx, _) =>
      ctx.thenPersist(UserCreated(username, email))(_ => ctx.reply(Done))
  }

  val updateUserHandler: OnCommandHandler[User] = {
    case (UpdateUser(username, email), ctx, Some(user)) =>
      val updatedUser: User = User(username.getOrElse(user.username), email.getOrElse(user.email))
      ctx.thenPersist(UserUpdated(updatedUser.username, updatedUser.email))(_ => ctx.reply(updatedUser))
  }

  val deleteUserHandler: OnCommandHandler[Done] = {
    case (DeleteUser, ctx, _) =>
      ctx.thenPersist(UserDeleted)(_ => ctx.reply(Done))
  }

  val onUserEvent: EventHandler = {
    case (UserCreated(username, email), _) =>
      Some(User(username, email))

    case (UserUpdated(updatedUsername, updatedEmail), Some(user)) =>
      Some(User(updatedUsername, updatedEmail))

    case (UserUpdated(_, _), None) =>
      None

    case (UserDeleted, _) =>
      None
  }
}

case class User(username: String, email: String)

object User {
  implicit val format: Format[User] = Json.format[User]
}