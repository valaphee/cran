/*
 * Copyright (c) 2022, Valaphee.
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

package com.valaphee.flow.node

import com.valaphee.flow.Scope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

/**
 * @author Kevin Ludwig
 */
abstract class State(
    type: String
) : Node(type) {
    private val jobs = mutableMapOf<Scope, Job>()

    abstract val inBegin: Int
    abstract val inAbort: Int
    abstract val outSubgraph: Int

    override fun initialize(scope: Scope) {
        val inBegin = scope.controlPath(inBegin)
        val inAbort = scope.controlPath(inAbort)
        val outSubgraph = scope.controlPath(outSubgraph)

        inBegin.declare {
            if (!jobs.containsKey(scope)) {
                with(CoroutineScope(coroutineContext)) { jobs[scope] = launch { outSubgraph() } }
                onBegin(scope)
                jobs.remove(scope)?.cancelAndJoin()
            }
        }
        inAbort.declare {
            onAbort(scope)
            jobs.remove(scope)?.cancelAndJoin()
        }
    }

    protected open suspend fun onBegin(scope: Scope) {}

    protected open suspend fun onAbort(scope: Scope) {}
}
