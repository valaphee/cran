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

package com.valaphee.cran

import com.valaphee.cran.graph.Graph
import com.valaphee.cran.hlsl.Constant
import com.valaphee.cran.hlsl.Declarable
import com.valaphee.cran.hlsl.Variable
import com.valaphee.cran.hlsl.Implementation
import com.valaphee.cran.path.DataPathException

/**
 * @author Kevin Ludwig
 */
class Hlsl(
    val impls: List<Implementation>,
    val graph: Graph
) {
    private var index = 0
    private val declarable = mutableMapOf<Int, Declarable>()

    fun defineConstant(dataPathId: Int, value: Any?) {
        declarable[dataPathId] = Constant(value)
    }

    fun defineVariable(dataPathId: Int, valueType: String, value: String) {
        declarable[dataPathId] = Variable("_${dataPathId}", index++, valueType, value)
    }

    fun declare(dataPathId: Int) = declarable[dataPathId]?.declare() ?: throw DataPathException.Undefined

    fun compile(): String {
        graph.sortedNodes.forEach { node -> impls.any { it.initialize(node, this) } }

        return """
            void main() {
                ${declarable.values.filterIsInstance<Variable>().sortedBy { it.index }.joinToString("\n") { "${it.declare()} = ${it.value}" }}
            }
        """.trimIndent()
    }
}
