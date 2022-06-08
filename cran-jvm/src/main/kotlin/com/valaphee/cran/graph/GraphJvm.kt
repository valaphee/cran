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

package com.valaphee.cran.graph

import com.valaphee.cran.node.NodeJvm
import com.valaphee.cran.node.nesting.ControlInput
import com.valaphee.cran.node.nesting.ControlOutput
import com.valaphee.cran.node.nesting.DataInput
import com.valaphee.cran.node.nesting.DataOutput

/**
 * @author Kevin Ludwig
 */
abstract class GraphJvm : Graph() {
    open fun initialize(scope: Scope) {
        nodes.forEach {
            when (it) {
                is NodeJvm<*> -> it.initialize(scope)
                else -> {
                    scope.graphLookup.getGraph(it.type)?.let { subGraph ->
                        val subScope = scope.subScope(subGraph).also { it.initialize() }
                        subGraph.nodes.forEach { subNode ->
                            when (subNode) {
                                is ControlInput -> {
                                    val out = subScope.controlPath(subNode.out)
                                    (it[subNode.json] as? Int)?.let {
                                        val `in` = scope.controlPath(it)
                                        out.function?.let(`in`::declare)
                                    }
                                }
                                is ControlOutput -> {
                                    val `in` = subScope.controlPath(subNode.`in`)
                                    (it[subNode.json] as? Int)?.let {
                                        val out = scope.controlPath(it)
                                        out.function?.let(`in`::declare)
                                    }
                                }
                                is DataInput -> {
                                    val out = subScope.dataPath(subNode.out)
                                    (it[subNode.json] as? Int)?.let {
                                        val `in` = scope.dataPath(it)
                                        `in`.valueFunction?.let(out::set) ?: out.set { `in`.get() }
                                    }
                                }
                                is DataOutput -> {
                                    val `in` = subScope.dataPath(subNode.`in`)
                                    (it[subNode.json] as? Int)?.let {
                                        val out = scope.dataPath(it)
                                        `in`.valueFunction?.let(out::set) ?: out.set { `in`.get() }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    open fun shutdown(scope: Scope) {
        //nodes.filterIsInstance<NodeJvm>().forEach { runBlocking { it.shutdown(scope) } }
    }
}
