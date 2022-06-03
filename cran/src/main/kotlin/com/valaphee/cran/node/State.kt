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

package com.valaphee.cran.node

import com.valaphee.cran.Scope
import com.valaphee.cran.path.ControlPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * @author Kevin Ludwig
 */
abstract class State(
    type: String
) : Node(type) {
    private val subgraphs = mutableMapOf<Scope, MutableList<Job>>()

    abstract val inBegin: Int
    abstract val inAbort: Int
    abstract val outOnBegin: Int
    abstract val outOnEnd: Int
    abstract val outSubgraph: Int

    override fun initialize(scope: Scope) {
        val inBegin = scope.controlPath(inBegin)
        val inAbort = scope.controlPath(inAbort)
        val outOnBegin = scope.controlPath(outOnBegin)
        val outOnEnd = scope.controlPath(outOnEnd)
        val outSubgraph = scope.controlPath(outSubgraph)

        inBegin.declare {
            if (!subgraphs.containsKey(scope)) {
                scope.invoke(outSubgraph)
                onBegin(scope)
                subgraphs.remove(scope)?.forEach { it.cancelAndJoin() }
                outOnEnd()
            }
        }
        inAbort.declare {
            subgraphs.remove(scope)?.forEach { it.cancelAndJoin() }
            outOnEnd()
        }
    }

    protected open suspend fun onBegin(scope: Scope) {}

    protected fun Scope.invoke(coroutineContext: CoroutineContext, subgraph: ControlPath) {
        if (subgraph.isDeclared) with(CoroutineScope(coroutineContext)) { subgraphs.getOrPut(this@invoke) { mutableListOf() } += launch { subgraph() } }
    }

    protected suspend fun Scope.invoke(subgraph: ControlPath) = invoke(kotlin.coroutines.coroutineContext, subgraph)
}
