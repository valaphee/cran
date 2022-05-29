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

package com.valaphee.flow.node.math.vector

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.Scope
import com.valaphee.flow.node.Node
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.NodeType
import com.valaphee.flow.spec.Out

/**
 * @author Kevin Ludwig
 */
@NodeType("Math/Vector/Add")
class Add(
    type: String,
    @get:In ("A"    , Vec) @get:JsonProperty("in_a") val inA: Int,
    @get:In ("B"    , Vec) @get:JsonProperty("in_b") val inB: Int,
    @get:Out("A + B", Vec) @get:JsonProperty("out" ) val out: Int
) : Node(type) {
    override fun initialize(scope: Scope) {
        val inA = scope.dataPath(inA)
        val inB = scope.dataPath(inB)
        val out = scope.dataPath(out)

        out.set { vectorOp(inA.get(), inB.get(), { a, b -> a.add(b) }, { a, b -> a.add(b) }, { a, b -> a.add(b) }, scope.objectMapper) }
    }
}