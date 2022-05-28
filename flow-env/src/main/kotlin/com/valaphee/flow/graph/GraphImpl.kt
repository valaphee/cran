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

package com.valaphee.flow.graph

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.Graph
import com.valaphee.flow.Scope
import com.valaphee.flow.meta.Meta
import com.valaphee.flow.node.Entry
import com.valaphee.flow.node.Node
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.Executors

/**
 * @author Kevin Ludwig
 */
class GraphImpl(
                               override val id   : UUID      ,
                               override val name : String    ,
    @get:JsonProperty("meta" )          val meta : Meta?     ,
                               override val nodes: List<Node>
) : Graph(), CoroutineScope {
    @JsonIgnore private val executor = Executors.newSingleThreadExecutor()
    @get:JsonIgnore override val coroutineContext get() = executor.asCoroutineDispatcher()

    override fun initialize(scope: Scope) {
        super.initialize(scope)
        nodes.forEach { if (it is Entry) launch { scope.controlPath(it.out)() } }
    }

    override fun shutdown(scope: Scope) {
        super.shutdown(scope)
        executor.shutdown()
    }
}
