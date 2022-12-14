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

package io.otavia.core.reactor

import io.netty5.util.internal.SystemPropertyUtil
import io.otavia.core.channel.Channel

/** [[Reactor]] is an io event generator for [[Channel]]. */
trait Reactor {

    /** API for [[io.otavia.core.actor.ChannelsActor]] to register [[Channel]] to [[Reactor]].
     *  @param channel
     *    [[Channel]] to listen io event.
     */
    def register(channel: Channel): Unit

    /** API for [[io.otavia.core.actor.ChannelsActor]] to deregister [[Channel]] to [[Reactor]].
     *  @param channel
     *    [[Channel]] to cancel listen io event.
     */
    def deregister(channel: Channel): Unit

}

object Reactor {

    val DEFAULT_MAX_TASKS_PER_RUN: Int =
        Math.max(1, SystemPropertyUtil.getInt("io.otavia.reactor.maxTaskPerRun", 1024 * 4))

}
