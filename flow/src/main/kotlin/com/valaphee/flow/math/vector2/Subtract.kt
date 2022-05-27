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

package com.valaphee.flow.math.vector2

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.Node
import com.valaphee.flow.Scope
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.NodeType
import com.valaphee.flow.spec.Out
import com.valaphee.foundry.math.Double2
import com.valaphee.foundry.math.Float2
import com.valaphee.foundry.math.Int2

/**
 * @author Kevin Ludwig
 */
@NodeType("Math/Vector 2/Subtract")
class Subtract(
    type: String,
    @get:In ("A"    , Vec2) @get:JsonProperty("in_a") val inA: Int,
    @get:In ("B"    , Vec2) @get:JsonProperty("in_b") val inB: Int,
    @get:Out("A - B", Vec2) @get:JsonProperty("out" ) val out: Int
) : Node(type) {
    override fun initialize(scope: Scope) {
        val inA = scope.dataPath(inA)
        val inB = scope.dataPath(inB)
        val out = scope.dataPath(out)

        out.set {
            val _inA = inA.get()
            val _inB = inB.get()
            when (_inA) {
                is Int2 -> when (_inB) {
                    is Int2    -> _inA             - _inB
                    is Float2  -> _inA.toFloat2()  - _inB
                    is Double2 -> _inA.toDouble2() - _inB
                    else       -> _inA.toDouble2() - inB.getOrThrow("in_b")
                }
                is Float2 -> when (_inB) {
                    is Int2    -> _inA             - _inB.toFloat2()
                    is Float2  -> _inA             - _inB
                    is Double2 -> _inA.toDouble2() - _inB
                    else       -> _inA.toDouble2() - inB.getOrThrow("in_b")
                }
                is Double2 -> when (_inB) {
                    is Int2    -> _inA - _inB.toDouble2()
                    is Float2  -> _inA - _inB.toDouble2()
                    is Double2 -> _inA - _inB
                    else       -> _inA - inB.getOrThrow("in_b")
                }
                else -> inA.getOrThrow<Double2>("in_a") - inB.getOrThrow("in_b")
            }
        }
    }
}
