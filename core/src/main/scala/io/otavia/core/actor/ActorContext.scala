/*
 * Copyright 2022 Yan Kun
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

import io.otavia.core.actor.Actor
import io.otavia.core.address.Address
import io.otavia.core.message.Message
import io.otavia.core.system.ActorSystem

/** [[Actor]] content info, the content info is create by actor system when a [[Actor]] instance is creating by actor
 *  system, and inject to [[Actor]] instance by [[Actor.setCtx]]
 *
 *  @param system
 *    actor system
 *  @param address
 *    physical address
 *  @param actorId
 *    id distributed by actor system
 */
final case class ActorContext(system: ActorSystem, address: Address[? <: Message], actorId: Long)
