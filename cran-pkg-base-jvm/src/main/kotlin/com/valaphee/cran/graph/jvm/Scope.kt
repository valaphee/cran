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

package com.valaphee.cran.graph.jvm

import com.fasterxml.jackson.databind.ObjectMapper
import com.valaphee.cran.node.NodeJvm
import com.valaphee.cran.path.jvm.ControlPath
import com.valaphee.cran.path.jvm.DataPath

/**
 * @author Kevin Ludwig
 */
class Scope(
    val objectMapper: ObjectMapper,
    val impls: List<NodeJvm>,
    val graphLookup: GraphJvmLookup,
    val graph: GraphJvm,
) {
    private val controlPaths = mutableListOf<ControlPath?>()
    private val dataPaths = mutableListOf<DataPath?>()

    fun subScope(graph: GraphJvm) = Scope(objectMapper, impls, graphLookup, graph)

    fun controlPath(controlPathId: Int) = controlPaths.getOrNull(controlPathId) ?: ControlPath().also {
        if (controlPathId > controlPaths.size) repeat(controlPathId - controlPaths.size) { controlPaths += null }
        if (controlPathId == controlPaths.size) controlPaths += it
        else controlPaths[controlPathId] = it
    }

    fun dataPath(dataPathId: Int) = dataPaths.getOrNull(dataPathId) ?: DataPath(objectMapper).also {
        if (dataPathId > dataPaths.size) repeat(dataPathId - dataPaths.size) { dataPaths += null }
        if (dataPathId == dataPaths.size) dataPaths += it
        else dataPaths[dataPathId] = it
    }

    fun initialize() {
        graph.initialize(this)
    }
}
