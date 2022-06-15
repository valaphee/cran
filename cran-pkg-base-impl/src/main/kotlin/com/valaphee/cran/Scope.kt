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

package com.valaphee.cran

import com.fasterxml.jackson.databind.ObjectMapper
import com.valaphee.cran.graph.Graph
import com.valaphee.cran.graph.GraphLookup
import com.valaphee.cran.impl.ControlPath
import com.valaphee.cran.impl.DataPath
import com.valaphee.cran.impl.Implementation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

/**
 * @author Kevin Ludwig
 */
class Scope(
    val objectMapper: ObjectMapper,
    val impls: List<Implementation>,
    val graphLookup: GraphLookup,
    val graph: Graph,
    coroutineContext: CoroutineContext
) : CoroutineScope {
    override val coroutineContext = coroutineContext + SupervisorJob()
    private val controlPaths = mutableListOf<ControlPath?>()
    private val dataPaths = mutableListOf<DataPath?>()

    fun subScope(graph: Graph) = Scope(objectMapper, impls, graphLookup, graph, coroutineContext)

    fun controlPath(pathId: Int) = controlPathOrNull(pathId) ?: ControlPath()

    fun controlPathOrNull(pathId: Int) = if (pathId == -1) null else controlPaths.getOrNull(pathId) ?: ControlPath().also {
        if (pathId > controlPaths.size) repeat(pathId - controlPaths.size) { controlPaths += null }
        if (pathId == controlPaths.size) controlPaths += it
        else controlPaths[pathId] = it
    }

    fun dataPath(pathId: Int) = dataPathOrNull(pathId) ?: DataPath(objectMapper)

    fun dataPathOrNull(pathId: Int) = if (pathId == -1) null else dataPaths.getOrNull(pathId) ?: DataPath(objectMapper).also {
        if (pathId > dataPaths.size) repeat(pathId - dataPaths.size) { dataPaths += null }
        if (pathId == dataPaths.size) dataPaths += it
        else dataPaths[pathId] = it
    }

    fun initialize() {
        val sortedNodes = graph.sortedNodes
        sortedNodes.forEach { node -> impls.any { impl -> impl.initialize(node, this) } }
        sortedNodes.forEach { node -> impls.any { impl -> impl.postInitialize(node, this) } }
    }
}
