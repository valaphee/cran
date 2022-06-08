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

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.cran.graph.Scope
import com.valaphee.cran.spec.NodeDecl
import com.valaphee.cran.spec.Out
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * @author Kevin Ludwig
 */
@NodeDecl("Entry")
class Entry(
    type: String,
    @get:Out("", "") @get:JsonProperty("out") val out: Int
) : Node(type), NodeJvm {
    private val jobs = mutableMapOf<Scope, Job>()

    override suspend fun shutdown(scope: Scope) {
        jobs[scope]?.cancelAndJoin()
    }

    suspend operator fun invoke(scope: Scope) {
        val out = scope.controlPath(out)

        if (!jobs.containsKey(scope)) coroutineScope { jobs[scope] = launch { out() }.also { it.invokeOnCompletion { jobs -= scope } } }
    }
}
