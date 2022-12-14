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

package io.otavia.core.stack

import io.otavia.core.message.{Ask, Message, Notice, Reply}
import io.otavia.core.stack.AsksFrame.ReturnType

final class ChannelFrame(initialState: StackState, val msgId: Long) {
    protected var stackState: StackState = initialState
    def state: StackState                = stackState
    private var r: AnyRef                = _
    private[core] var pre: ChannelFrame  = null
    private[core] var next: ChannelFrame = null

    final private[core] def nextState(stackState: StackState): Unit = this.stackState = stackState

    def `return`(reply: AnyRef): None.type = {
        r = reply
        None
    }

    def `return`(): None.type = {
        r = None
        None
    }

}

sealed abstract class StackFrame(initialState: StackState) {
    protected var stackState: StackState = initialState
    protected var error: Boolean         = false

    def state: StackState = stackState

    final private[core] def nextState(stackState: StackState): Unit = this.stackState = stackState

    def call: Message | Seq[Message]

    def `return`(): Unit = {}

    def `return`(reply: Reply): None.type = None

    protected[core] def setError(): Unit = error = true
    protected[core] def thrown: Boolean  = error

    def isCompleted: Boolean
}

sealed abstract class NoReturnFrame(initialState: StackState) extends StackFrame(initialState) {
    private var completed: Boolean = false

    override def `return`(): Unit = completed = true

    override def isCompleted: Boolean = completed
}

final class NoticeFrame private[core] (val notice: Notice, initialState: StackState = null)
    extends NoReturnFrame(initialState) {
    override def call: Notice = notice
}

final class NoticesFrame private[core] (val notices: Seq[Notice], initialState: StackState = null)
    extends NoReturnFrame(initialState) {
    override def call: Seq[Notice] = notices
}

sealed abstract class ReturnFrame(initialState: StackState) extends StackFrame(initialState) {
    protected var r: Reply = _

    def isCompleted: Boolean = r != null

    def reply: Reply = r
}

final class AskFrame private[core] (val ask: Ask[?], initialState: StackState = null)
    extends ReturnFrame(initialState) {
    override def call: Ask[?] = ask
    override def `return`(reply: Reply): None.type = {
        reply.setReplyId(ask.id)
        this.r = reply
        None
    }
}

final class AsksFrame private[core] (val asks: Seq[Ask[?]], initialState: StackState = null)
    extends ReturnFrame(initialState) {
    var returnType: AsksFrame.ReturnType = _
    override def call: Seq[Ask[?]]       = asks
    override def `return`(reply: Reply): None.type = {
        reply.setReplyId(asks.map(_.id))
        this.r = reply
        returnType = AsksFrame.ReturnType.ALL_FOR_ONE
        None
    }

    def `return`(replies: Map[Long, Reply]): Unit = ???

}

object AsksFrame {
    enum ReturnType {
        case ALL_FOR_ONE
        case ONE_BY_ONE
    }
}
