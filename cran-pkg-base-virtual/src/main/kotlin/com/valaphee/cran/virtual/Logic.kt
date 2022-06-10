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

package com.valaphee.cran.virtual

import com.valaphee.cran.Virtual
import com.valaphee.cran.node.BinaryOperation
import com.valaphee.cran.node.Node
import com.valaphee.cran.node.UnaryOperation
import com.valaphee.cran.node.logic.And
import com.valaphee.cran.node.logic.Not
import com.valaphee.cran.node.logic.Or
import com.valaphee.cran.spec.NodeImpl
import kotlinx.coroutines.CoroutineScope

/**
 * @author Kevin Ludwig
 */
@NodeImpl("virtual")
object Logic : Implementation {
    override fun initialize(coroutineScope: CoroutineScope, node: Node, virtual: Virtual) = when (node) {
        is UnaryOperation -> {
            val `in` = virtual.dataPath(node.`in`)
            val out = virtual.dataPath(node.out)

            when (node) {
                is Not -> {
                    out.set { !`in`.getOfType<Boolean>() }

                    true
                }
                else -> false
            }
        }
        is BinaryOperation -> {
            val in1 = virtual.dataPath(node.in1)
            val in2 = virtual.dataPath(node.in2)
            val out = virtual.dataPath(node.out)

            when (node) {
                is Or -> {
                    out.set { in1.getOfType() || in2.getOfType() }

                    true
                }
                is And -> {
                    out.set { in1.getOfType() && in2.getOfType() }

                    true
                }
                else ->  false
            }
        }
        else -> false
    }
}
