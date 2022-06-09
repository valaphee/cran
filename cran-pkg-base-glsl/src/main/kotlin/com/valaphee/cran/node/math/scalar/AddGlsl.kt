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

package com.valaphee.cran.node.math.scalar

import com.valaphee.cran.graph.glsl.Scope
import com.valaphee.cran.node.Node
import com.valaphee.cran.spec.NodeImpl

/**
 * @author Kevin Ludwig
 */
@NodeImpl("glsl")
object AddGlsl {
    fun generate(node: Node, scope: Scope) = if (node is Add) {
        val inA = scope.variable(node.inA)
        val inB = scope.variable(node.inB)

        scope.define(node.out, "add_${node.out}", "${inA.declare()} + ${inB.declare()}", inA, inB)

        true
    } else false
}
