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

package com.valaphee.flow.node.logic

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.Scope
import com.valaphee.flow.node.Bit
import com.valaphee.flow.node.Node
import com.valaphee.flow.node.Und
import com.valaphee.flow.path.DataPathException
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.NodeType
import com.valaphee.flow.spec.Out

/**
 * @author Kevin Ludwig
 */
@NodeType("Logic/Less Than or Equal")
class LessThanOrEqual(
    type: String,
    @get:In ("A"    , Und) @get:JsonProperty("in_a") val inA: Int,
    @get:In ("B"    , Und) @get:JsonProperty("in_b") val inB: Int,
    @get:Out("A ≤ B", Bit) @get:JsonProperty("out" ) val out: Int
) : Node(type) {
    override fun initialize(scope: Scope) {
        val inA = scope.dataPath(inA)
        val inB = scope.dataPath(inB)
        val out = scope.dataPath(out)

        out.set {
            val _inA = inA.get()
            val _inB = inB.get()
            val _out = Compare.compare(_inA, _inB)
            if (_out != Int.MAX_VALUE) _out <= 0 else DataPathException.invalidExpression("$_inA ≤ $_inB")
        }
    }
}
