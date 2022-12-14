/*
 * Copyright 2022 Yan Kun <yan_kun_1992@foxmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.otavia.core.actor

import io.otavia.core.actor.AccepterActor.*
import io.otavia.core.address.Address
import io.otavia.core.channel.*
//import io.otavia.core.channel.impl.NioServerSocketChannel
import io.otavia.core.message.*
import io.otavia.core.reactor.RegisterReplyEvent
import io.otavia.core.stack.{ChannelFrame, ExceptionWaiter, ReplyWaiter, StackState}

import java.net.{InetAddress, InetSocketAddress, SocketAddress}
import scala.runtime.Nothing$

abstract class AccepterActor[W <: AcceptedWorkerActor[_ <: Ask[?] | Notice]] extends ChannelsActor[Bind] {

    val workerFactory: WorkerFactory[W]
    private var localAddress: SocketAddress    = _
    private var bound: Boolean                 = false
    private var workers: Address[MessageOf[W]] = _

    /** Number of worker. */
    protected def workerNumber: Int = 1

    override def afterMount(): Unit = {
        workers = system.crateActor(workerFactory, workerNumber)
    }

    protected def bind(port: Int): Unit                    = bind(new InetSocketAddress(port))
    protected def bind(host: String, port: Int): Unit      = bind(InetSocketAddress.createUnresolved(host, port))
    protected def bind(host: InetAddress, port: Int): Unit = bind(new InetSocketAddress(host, port))
    protected def bind(localAddress: SocketAddress): Channel = {
        assert(!bound)
        this.localAddress = localAddress
        // 1. create channel
        // 2. init: pipeline and config, add ServerBootstrapAcceptor handler
        // 3. register to reactor ...
        initAndRegister()
    }

    override def init(channel: Channel): Unit = {
        if (handler.nonEmpty) {
            channel.pipeline.addLast(handler.get)
        }
        channel.pipeline.addLast(new AccepterHandler)
    }

    private[core] final override def receiveRegisterReply(event: RegisterReplyEvent): Unit = {
        // 3. ..., then set channel state registered and fireChannelRegistered
        // 4. bind then set channel state active and fireChannelActive
        event.channel.pipeline.fireChannelRegistered()

        event.channel.pipeline.bind(localAddress)
        bound = true
        event.channel.pipeline.fireChannelActive()
    }

    final override def continueChannelMessage(msg: AnyRef | ChannelFrame): Option[StackState] = msg match
        case accepted: Channel =>
            val state = new DispatchState()
            workers.ask[AcceptedChannel](AcceptedChannel(accepted), state.dispatchWaiter)
            Some(state)
        case frame: ChannelFrame =>
            frame.`return`()

}

object AccepterActor {

    trait WorkerFactory[W <: AcceptedWorkerActor[_ <: Ask[?] | Notice]] extends ActorFactory[W] {
        override def newActor(): W
    }

    final case class AcceptedChannel(channel: Channel)(using IdAllocator) extends Ask[UnitReply]

    final case class Bind(localAddress: SocketAddress)(using IdAllocator) extends Ask[UnitReply], Notice
    object Bind {

        def apply(port: Int)(using IdAllocator): Bind = Bind(new InetSocketAddress(port))
        def apply(host: String, port: Int)(using IdAllocator): Bind = Bind(
          InetSocketAddress.createUnresolved(host, port)
        )
        def apply(host: InetAddress, port: Int)(using IdAllocator): Bind = Bind(new InetSocketAddress(host, port))

    }

    private class AccepterHandler extends ChannelHandler {
        override def channelRead(ctx: ChannelHandlerContext, msg: AnyRef): Unit = {
            val accepted = msg.asInstanceOf[Channel]
            val msgId    = ctx.channel.generateMessageId
            ctx.fireChannelRead(accepted, msgId)
        }
    }

    final class DispatchState extends StackState {

        val dispatchWaiter = new ReplyWaiter[UnitReply]()

        override def resumable(): Boolean = dispatchWaiter.received

    }

}
