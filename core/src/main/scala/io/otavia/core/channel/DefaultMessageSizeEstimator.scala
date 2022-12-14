/*
 * Copyright 2022 Yan Kun <yan_kun_1992@foxmail.com>
 *
 * This file fork from netty.
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

package io.otavia.core.channel

import io.netty5.buffer.Buffer

/** Default [[MessageSizeEstimator]] implementation which supports the estimation of the size of [[Buffer]] and
 *  [[FileRegion]].
 */
private object DefaultMessageSizeEstimator extends MessageSizeEstimator {

    private final val handle = new HandleImpl(8)

    /** Creates a new handle. The handle provides the actual operations. */
    override def newHandle: MessageSizeEstimator.Handle = handle

    final private class HandleImpl(private val unknownSize: Int) extends MessageSizeEstimator.Handle {
        override def size(msg: AnyRef): Int = msg match
            case buffer: Buffer => return buffer.readableBytes
            case _: FileRegion  => 0
            case _              => unknownSize
    }
}
