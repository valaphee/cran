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

package com.valaphee.cran.node.input

import com.valaphee.cran.graph.jvm.Scope
import com.valaphee.cran.node.Node
import com.valaphee.cran.spec.NodeImpl

/**
 * @author Kevin Ludwig
 */
@NodeImpl("jvm")
object SetMouseButtonJvm : MouseNodeJvm() {
    override fun initialize(node: Node, scope: Scope) = if (node is SetMouseButton) {
        val `in` = scope.controlPath(node.`in`)
        val inButton = scope.dataPath(node.inButton)
        val inState = scope.dataPath(node.inState)
        val out = scope.controlPath(node.out)

        `in`.define {
            val button = inButton.getOfType<Int>()
            if (inState.getOfType()) {
                buttons.set(button)
                write()
            } else {
                buttons.clear(button)
                write()
            }
            out()
        }

        true
    } else false
}
