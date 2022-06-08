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

package com.valaphee.cran.node.control

import com.valaphee.cran.graph.Scope
import com.valaphee.cran.node.NodeJvm
import com.valaphee.cran.spec.NodeDef

/**
 * @author Kevin Ludwig
 */
@NodeDef("jvm", For::class)
object ForJvm : NodeJvm<For> {
    override fun initialize(node: For, scope: Scope) {
        val `in` = scope.controlPath(node.`in`)
        val inRangeStart = scope.dataPath(node.inRangeStart)
        val inRangeEnd = scope.dataPath(node.inRangeEnd)
        val inStep = scope.dataPath(node.inStep)
        val outBody = scope.controlPath(node.outBody)
        val out = scope.controlPath(node.out)
        val outIndex = scope.dataPath(node.outIndex)

        `in`.declare {
            IntProgression.fromClosedRange(inRangeStart.getOfType(), inRangeEnd.getOfType(), inStep.getOfType()).forEach {
                outIndex.set(it)
                outBody()
            }
            out()
        }
    }
}
