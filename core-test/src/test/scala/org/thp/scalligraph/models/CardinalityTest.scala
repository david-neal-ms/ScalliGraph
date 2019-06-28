package org.thp.scalligraph.models

import scala.util.{Success, Try}

import play.api.libs.logback.LogbackLoggerConfigurator
import play.api.test.PlaySpecification
import play.api.{Configuration, Environment}

import gremlin.scala.{Graph, GremlinScala, Key, Vertex}
import org.specs2.mock.Mockito
import org.specs2.specification.core.Fragments
import org.thp.scalligraph.VertexEntity
import org.thp.scalligraph.auth.{AuthContext, UserSrv}
import org.thp.scalligraph.services.VertexSrv

@VertexEntity
case class EntityWithSeq(name: String, valueList: Seq[String], valueSet: Set[String])

class EntityWithSeqSrv(implicit db: Database) extends VertexSrv[EntityWithSeq, VertexSteps[EntityWithSeq]] {
  override def steps(raw: GremlinScala[Vertex])(implicit graph: Graph): VertexSteps[EntityWithSeq] = new VertexSteps[EntityWithSeq](raw)

  def getFromKey(key: String, value: String)(implicit graph: Graph): Try[EntityWithSeq] =
    new VertexSteps[EntityWithSeq](initSteps.raw.has(Key(key) of value)).getOrFail()
}

class CardinalityTest extends PlaySpecification with Mockito {

  val userSrv: UserSrv                  = DummyUserSrv()
  implicit val authContext: AuthContext = userSrv.initialAuthContext
  (new LogbackLoggerConfigurator).configure(Environment.simple(), Configuration.empty, Map.empty)

  Fragments.foreach(new DatabaseProviders().list) { dbProvider =>
    implicit val db: Database = dbProvider.get()
    db.createSchema(db.getModel[EntityWithSeq])
    val entityWithSeqSrv: EntityWithSeqSrv = new EntityWithSeqSrv

    s"[${dbProvider.name}] entity" should {
      "create with empty list and set" in db.transaction { implicit graph =>
        val initialEntity                            = EntityWithSeq("The answer", Seq.empty, Set.empty)
        val createdEntity: EntityWithSeq with Entity = entityWithSeqSrv.create(initialEntity)
        createdEntity._id must_!== null
        initialEntity must_=== createdEntity
        entityWithSeqSrv.getOrFail(createdEntity._id) must beSuccessfulTry(createdEntity)
      }

      "create and get entities with list property" in db.transaction { implicit graph =>
        val initialEntity                            = EntityWithSeq("list", Seq("1", "2", "3"), Set.empty)
        val createdEntity: EntityWithSeq with Entity = entityWithSeqSrv.create(initialEntity)
        initialEntity must_=== createdEntity
        entityWithSeqSrv.getOrFail(createdEntity._id) must beSuccessfulTry(createdEntity)
      }

      "create and get entities with set property" in db.transaction { implicit graph =>
        val initialEntity                            = EntityWithSeq("list", Seq.empty, Set("a", "b", "c"))
        val createdEntity: EntityWithSeq with Entity = entityWithSeqSrv.create(initialEntity)
        initialEntity must_=== createdEntity
        entityWithSeqSrv.getOrFail(createdEntity._id) must_=== Success(createdEntity)
      }

      "be searchable from its list property" in db.transaction { implicit graph =>
        val initialEntity                            = EntityWithSeq("list", Seq("1", "2", "3"), Set.empty)
        val createdEntity: EntityWithSeq with Entity = entityWithSeqSrv.create(initialEntity)
        entityWithSeqSrv.getFromKey("valueList", "1") must beSuccessfulTry(createdEntity)
      // This test fails with OrientDB : https://github.com/orientechnologies/orientdb-gremlin/issues/120
      }

//      "update an entity" in db.transaction { implicit graph ⇒
//        val id = entityWithSeqSrv.create(EntityWithSeq("super", 7))._id
//        entityWithSeqSrv.update(id, "value", 8)
//
//        entityWithSeqSrv.getOrFail(id).value must_=== 8
//      }
    }
  }
}
