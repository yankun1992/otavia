/*
 * Copyright 2022 Yan Kun
 *
 * This class is fork from netty
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

import java.util.concurrent.{Executor, ThreadFactory}

class LoopExecutor(private val threadFactory: ThreadFactory) extends Executor {
    override def execute(command: Runnable): Unit = threadFactory.newThread(command).start()
}

object LoopExecutor {

    def apply(threadFactory: ThreadFactory): LoopExecutor = new LoopExecutor(threadFactory)
    def apply(): LoopExecutor                             = new LoopExecutor(threadFactory)
    private object DefaultThreadFactory extends ThreadFactory {
        override def newThread(r: Runnable): Thread = {
            val thread = new Thread(r, "")
            try {
                if (thread.isDaemon) thread.setDaemon(false)
                if (thread.getPriority != Thread.NORM_PRIORITY) thread.setPriority(Thread.NORM_PRIORITY)
            } catch { case ignore: Exception => }
            thread
        }
    }

    private def threadFactory: ThreadFactory = DefaultThreadFactory

}
