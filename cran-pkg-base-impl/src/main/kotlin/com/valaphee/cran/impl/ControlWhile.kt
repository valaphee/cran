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

package com.valaphee.cran.impl

import com.valaphee.cran.Scope
import com.valaphee.cran.node.Node
import com.valaphee.cran.node.control.While
import com.valaphee.cran.spec.NodeImpl

/**
 * @author Kevin Ludwig
 */
@NodeImpl
object ControlWhile : Implementation {
    override fun initialize(node: Node, scope: Scope) = if (node is While) {
        val `in` = scope.controlPath(node.`in`)
        val inValue = scope.dataPath(node.inValue)
        val outBody = scope.controlPath(node.outBody)
        val out = scope.controlPath(node.out)

        `in`.define {
            while (inValue.getOfType()) outBody()
            out()
        }

        true
    } else false
}
