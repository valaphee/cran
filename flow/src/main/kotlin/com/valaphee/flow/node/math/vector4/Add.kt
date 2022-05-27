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

package com.valaphee.flow.node.math.vector4

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.Scope
import com.valaphee.flow.node.Node
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.NodeType
import com.valaphee.flow.spec.Out
import com.valaphee.foundry.math.Double4
import com.valaphee.foundry.math.Float4
import com.valaphee.foundry.math.Int4

/**
 * @author Kevin Ludwig
 */
@NodeType("Math/Vector 4/Add")
class Add(
    type: String,
    @get:In ("A"    , Vec4) @get:JsonProperty("in_a") val inA: Int,
    @get:In ("B"    , Vec4) @get:JsonProperty("in_b") val inB: Int,
    @get:Out("A + B", Vec4) @get:JsonProperty("out" ) val out: Int
) : Node(type) {
    override fun initialize(scope: Scope) {
        val inA = scope.dataPath(inA)
        val inB = scope.dataPath(inB)
        val out = scope.dataPath(out)

        out.set {
            val _inA = inA.get()
            val _inB = inB.get()
            when (_inA) {
                is Int4 -> when (_inB) {
                    is Int4    -> _inA             + _inB
                    is Float4  -> _inA.toFloat4()  + _inB
                    is Double4 -> _inA.toDouble4() + _inB
                    else       -> _inA.toDouble4() + inB.getOrThrow("in_b")
                }
                is Float4 -> when (_inB) {
                    is Int4    -> _inA             + _inB.toFloat4()
                    is Float4  -> _inA             + _inB
                    is Double4 -> _inA.toDouble4() + _inB
                    else       -> _inA.toDouble4() + inB.getOrThrow("in_b")
                }
                is Double4 -> when (_inB) {
                    is Int4    -> _inA + _inB.toDouble4()
                    is Float4  -> _inA + _inB.toDouble4()
                    is Double4 -> _inA + _inB
                    else       -> _inA + inB.getOrThrow("in_b")
                }
                else -> inA.getOrThrow<Double4>("in_a") + inB.getOrThrow("in_b")
            }
        }
    }
}
