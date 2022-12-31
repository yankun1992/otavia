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

package io.otavia.core.timer

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

private[core] class TimerThreadFactory extends ThreadFactory {

    private val threadNumber: AtomicInteger = new AtomicInteger(0)
    private val namePrefix: String          = "otavia-timer-"

    override def newThread(r: Runnable): Thread = {
        val t = new Thread(null, r, namePrefix + threadNumber.getAndIncrement(), 0)
        if (t.isDaemon) t.setDaemon(false)
        if (t.getPriority != Thread.NORM_PRIORITY) t.setPriority(Thread.NORM_PRIORITY)
        t
    }

}
