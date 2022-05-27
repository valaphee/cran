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

package com.valaphee.flow.logic

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.Bit
import com.valaphee.flow.Scope
import com.valaphee.flow.Node
import com.valaphee.flow.Und
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.NodeType
import com.valaphee.flow.spec.Out

/**
 * @author Kevin Ludwig
 */
@NodeType("Logic/Not Equal")
class NotEqual(
    @get:In ("A"    , Und) @get:JsonProperty("in_a") val inA: Int,
    @get:In ("B"    , Und) @get:JsonProperty("in_b") val inB: Int,
    @get:Out("A ≠ B", Bit) @get:JsonProperty("out" ) val out: Int
) : Node() {
    override fun initialize(scope: Scope) {
        val inA = scope.dataPath(inA)
        val inB = scope.dataPath(inB)
        val out = scope.dataPath(out)

        out.set {
            val _inA = inA.get()
            val _inB = inB.get()
            _inA != _inB
        }
    }
}
