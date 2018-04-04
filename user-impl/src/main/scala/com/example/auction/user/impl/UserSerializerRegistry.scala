package com.example.auction.user.impl

import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

object UserSerializerRegistry extends JsonSerializerRegistry {
  override def serializers = List(
    JsonSerializer[User],
    JsonSerializer[UserCreated],
    JsonSerializer[UserUpdated],
    JsonSerializer[UserDeleted.type],
    JsonSerializer[CreateUser],
    JsonSerializer[UpdateUser],
    JsonSerializer[DeleteUser.type],
    JsonSerializer[GetUser.type]
  )
}