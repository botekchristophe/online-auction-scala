package com.example.auction.user.impl

import java.util.UUID

import akka.NotUsed
import akka.actor.ActorSystem
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.persistence.query.PersistenceQuery
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.example.auction.user.api
import com.example.auction.user.api._
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.transport.NotFound
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.concurrent.ExecutionContext

class UserServiceImpl(registry: PersistentEntityRegistry, system: ActorSystem)
                     (implicit ec: ExecutionContext, mat: Materializer) extends UserService {

  private val currentIdsQuery = PersistenceQuery(system).readJournalFor[CassandraReadJournal](CassandraReadJournal.Identifier)

  override def createUser: ServiceCall[UserCreationRequest, api.User] =
    ServiceCall { createUser =>
      val userId = UUID.randomUUID()
      refFor(userId)
        .ask(CreateUser(createUser.username, createUser.email))
        .map(_ => api.User(userId, createUser.username, createUser.email))
    }

  override def getUser(id: UUID): ServiceCall[NotUsed, api.User] =
    ServiceCall { _ =>
      refFor(id)
        .ask(GetUser)
        .map {
          case Some(user) =>
            api.User(id, user.username, user.email)
          case None =>
            throw NotFound(s"User with id $id")
        }
    }

  override def getUsers = ServiceCall { _ =>
    // Note this should never make production....
    currentIdsQuery.currentPersistenceIds()
      .filter(_.startsWith("UserEntity|"))
      .mapAsync(4) { id =>
        val entityId = id.split("\\|", 2).last
        registry.refFor[UserEntity](entityId)
          .ask(GetUser)
          .map(_.map(user => api.User(UUID.fromString(entityId), user.username, user.email)))
      }.collect {
      case Some(user) => user
    }
      .runWith(Sink.seq)
  }

  override def updateUser(id: UUID): ServiceCall[UserUpdateRequest, api.User] =
    ServiceCall(updateUser =>
      refFor(id)
        .ask(UpdateUser(updateUser.username, updateUser.email))
        .map(updatedUser => api.User(id, updatedUser.username, updatedUser.email))
    )

  override def deleteUser(id: UUID): ServiceCall[NotUsed, NotUsed] =
    ServiceCall(_ =>
      refFor(id)
        .ask(DeleteUser)
        .map(_ => NotUsed)
    )

  private def refFor(userId: UUID) = registry.refFor[UserEntity](userId.toString)
}