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

import com.valaphee.cran.Virtual
import com.valaphee.cran.node.Node
import com.valaphee.cran.node.NodeVirtual
import com.valaphee.cran.spec.NodeImpl

/**
 * @author Kevin Ludwig
 */
@NodeImpl("virtual")
object SubGraphVirtual : NodeVirtual {
    override fun initialize(node: Node, virtual: Virtual) = virtual.graphLookup.getGraph(node.type)?.let { subGraph ->
        val sub = virtual.sub(subGraph).also { it.initialize() }
        subGraph.nodes.forEach { subNode ->
            when (subNode) {
                is ControlInput -> {
                    val out = sub.controlPath(subNode.out)
                    (node[subNode.json] as? Int)?.let {
                        val `in` = virtual.controlPath(it)
                        out.function?.let(`in`::define)
                    }
                }
                is ControlOutput -> {
                    val `in` = sub.controlPath(subNode.`in`)
                    (node[subNode.json] as? Int)?.let {
                        val out = virtual.controlPath(it)
                        out.function?.let(`in`::define)
                    }
                }
                is DataInput -> {
                    val out = sub.dataPath(subNode.out)
                    (node[subNode.json] as? Int)?.let {
                        val `in` = virtual.dataPath(it)
                        `in`.valueFunction?.let(out::set) ?: out.set { `in`.get() }
                    }
                }
                is DataOutput -> {
                    val `in` = sub.dataPath(subNode.`in`)
                    (node[subNode.json] as? Int)?.let {
                        val out = virtual.dataPath(it)
                        `in`.valueFunction?.let(out::set) ?: out.set { `in`.get() }
                    }
                }
            }
        }

        true
    } ?: false
}
