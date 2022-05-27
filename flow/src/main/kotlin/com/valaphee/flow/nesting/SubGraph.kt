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

package com.valaphee.flow.nesting

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.valaphee.flow.Graph
import com.valaphee.flow.Node
import com.valaphee.flow.Scope

/**
 * @author Kevin Ludwig
 */
class SubGraph(
    type: String
) : Node(type) {
    @get:JsonAnyGetter val paths = mutableMapOf<String, Int>()

    @JsonAnySetter
    fun setPath(key: String, value: Int) {
        paths[key] = value
    }

    override fun initialize(scope: Scope) {
        val subGraph = checkNotNull(Graph.graphs.find { it.name == type })
        val subScope = Scope()
        subGraph.initialize(subScope)
        subGraph.nodes.forEach {
            when (it) {
                is ControlInput -> {
                    val out = subScope.controlPath(it.out)
                    paths[it.json]?.let {
                        val `in` = scope.controlPath(it)
                        `in`.declare { out() }
                    }
                }
                is ControlOutput -> {
                    val `in` = subScope.controlPath(it.`in`)
                    paths[it.json]?.let {
                        val out = scope.controlPath(it)
                        `in`.declare { out() }
                    }
                }
                is DataInput -> {
                    val out = subScope.dataPath(it.out)
                    paths[it.json]?.let {
                        val `in` = scope.dataPath(it)
                        out.set { `in`.get() }
                    }
                }
                is DataOutput -> {
                    val `in` = subScope.dataPath(it.`in`)
                    paths[it.json]?.let {
                        val out = scope.dataPath(it)
                        out.set { `in`.get() }
                    }
                }
            }
        }
    }
}
