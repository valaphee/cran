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

package com.valaphee.cran.node.nesting

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.valaphee.cran.Scope
import com.valaphee.cran.node.Node

/**
 * @author Kevin Ludwig
 */
class Subgraph(
    type: String
) : Node(type) {
    @get:JsonAnyGetter val paths = mutableMapOf<String, Int>()

    @JsonAnySetter
    fun setPath(key: String, value: Int) {
        paths[key] = value
    }

    override fun initialize(scope: Scope) {
        val subGraph = checkNotNull(scope.graphManager.getGraph(type))
        val subScope = scope.subScope(subGraph).also { it.initialize() }
        subGraph.nodes.forEach {
            when (it) {
                is ControlInput -> {
                    val out = subScope.controlPath(it.out)
                    paths[it.json]?.let {
                        val `in` = scope.controlPath(it)
                        out.function?.let(`in`::declare)
                    }
                }
                is ControlOutput -> {
                    val `in` = subScope.controlPath(it.`in`)
                    paths[it.json]?.let {
                        val out = scope.controlPath(it)
                        out.function?.let(`in`::declare)
                    }
                }
                is DataInput -> {
                    val out = subScope.dataPath(it.out)
                    paths[it.json]?.let {
                        val `in` = scope.dataPath(it)
                        `in`.valueFunction?.let(out::set) ?: out.set { `in`.get() }
                    }
                }
                is DataOutput -> {
                    val `in` = subScope.dataPath(it.`in`)
                    paths[it.json]?.let {
                        val out = scope.dataPath(it)
                        `in`.valueFunction?.let(out::set) ?: out.set { `in`.get() }
                    }
                }
            }
        }
    }
}
