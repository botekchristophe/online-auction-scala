package com.example.auction.user.impl

import com.example.auction.utils.JsonFormats.singletonFormat
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventShards, AggregateEventTag, AggregateEventTagger}
import play.api.libs.json.{Format, Json}


trait UserEvent extends AggregateEvent[UserEvent] {
  override def aggregateTag: AggregateEventTagger[UserEvent] = UserEvent.Tag
}

object UserEvent {
  val NumShards = 4
  val Tag: AggregateEventShards[UserEvent] = AggregateEventTag.sharded[UserEvent](NumShards)
}

case class UserCreated(username: String, email: String) extends UserEvent

object UserCreated {
  implicit val format: Format[UserCreated] = Json.format[UserCreated]
}

case class UserUpdated(username: String, email: String) extends UserEvent

object UserUpdated {
  implicit val format: Format[UserUpdated] = Json.format[UserUpdated]
}

case object UserDeleted extends UserEvent {
  implicit val format: Format[UserDeleted.type] = singletonFormat(UserDeleted)
}