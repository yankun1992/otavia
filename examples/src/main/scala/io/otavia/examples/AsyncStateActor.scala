package io.otavia.examples

import io.otavia.core.actor.NormalActor
import io.otavia.core.address.Address
import io.otavia.core.async.Async
import io.otavia.core.ioc.Injectable
import io.otavia.core.message.{Ask, Message, Notice, Reply, ExceptionMessage}
import io.otavia.core.stack.*
import io.otavia.core.stack
import io.otavia.examples.HandleStateActor.*
import scala.annotation.experimental
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@experimental
class AsyncStateActor extends NormalActor[MSG] with Injectable {

  var redis: Address[QueryRedis] = _
  var db: Address[QueryDB] = _

  override def afterMount(): Unit = {
    redis = autowire("redis-client")
    db = autowire("database-client")
  }

  def showExpr(msg: MSG): Any = Async.async {
    msg match
      case request: Request =>
        val variable = 1 + 2
        val variable2: Int = 1 + 2
        val queryRedis = QueryRedis(request.req)
        val redisResponse: RedisResponse | ExceptionMessage = Async.await(redis, queryRedis)
        val p = {
          val variable3 = variable + variable2
          val redisRes: RedisResponse = Async.await(redis, queryRedis)
          variable3
        }
        Future {
          println("hello")
        }
        if (redisResponse.isInstanceOf[RedisResponse] && redisResponse.asInstanceOf[RedisResponse].res == "null") {
          val dbResponse: DBResponse = Async.await(db, QueryDB(request.req))
          val res = Response(s"hit in database with result: ${dbResponse.res}")
          request.reply(res)
        } else {
          request.reply(Response(s"hit in redis with result: ${redisResponse.asInstanceOf[RedisResponse].res}"))
        }
        None
  }

  override def continueAsk(msg: MSG & Ask[?] | AskFrame): Option[StackState] = Async.async {
    val aaa: Int = 1
    // this will auto generate by Macros.async
    final class State1(var query: String) extends StackState {
      val redisWaiter: ReplyWaiter[RedisResponse] = new ReplyWaiter()
      override def resumable(): Boolean = redisWaiter.received
    }
    // this will auto generate by Macros.async
    final class State2 extends StackState {
      val dbWaiter: ReplyWaiter[DBResponse] = new ReplyWaiter()
      override def resumable(): Boolean = dbWaiter.received
    }

    msg match {
      case request: Request =>
        val state = new State1(request.req)
        redis.ask(QueryRedis(request.req),state.redisWaiter)
        Some(state)
      case stackFrame: StackFrame =>
        stackFrame.state match
          case state: State1 =>
            val redisResponse = state.redisWaiter.reply
            if (redisResponse.res == "null") {
              val dbState = new State2()
              db.ask[QueryDB](QueryDB(state.query),dbState.dbWaiter)
              Some(dbState)
            } else {
              stackFrame.`return`(Response(s"hit in redis with result: ${redisResponse.res}"))
            }
          case dbState: State2 =>
            stackFrame.`return`(Response(s"hit in database with result: ${dbState.dbWaiter.reply.res}"))
    }
  }

}
