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
import com.valaphee.cran.node.Node
import com.valaphee.cran.node.control.For
import com.valaphee.cran.spec.NodeImpl
import kotlinx.coroutines.CoroutineScope

/**
 * @author Kevin Ludwig
 */
@NodeImpl("virtual")
object ControlFor : Implementation {
    override fun initialize(coroutineScope: CoroutineScope, node: Node, virtual: Virtual) = if (node is For) {
        val `in` = virtual.controlPath(node.`in`)
        val inRangeStart = virtual.dataPath(node.inRangeStart)
        val inRangeEnd = virtual.dataPath(node.inRangeEnd)
        val inStep = virtual.dataPath(node.inStep)
        val outBody = virtual.controlPath(node.outBody)
        val out = virtual.controlPath(node.out)
        val outIndex = virtual.dataPath(node.outIndex)

        `in`.define {
            IntProgression.fromClosedRange(inRangeStart.getOfType(), inRangeEnd.getOfType(), inStep.getOfType()).forEach {
                outIndex.set(it)
                outBody()
            }
            out()
        }

        true
    } else false
}
