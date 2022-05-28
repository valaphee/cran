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
import com.google.common.math.IntMath
import com.valaphee.flow.Scope
import com.valaphee.flow.node.Node
import com.valaphee.flow.node.Num
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.NodeType
import com.valaphee.flow.spec.Out
import jdk.incubator.vector.VectorOperators
import java.math.RoundingMode
import kotlin.math.sqrt

/**
 * @author Kevin Ludwig
 */
@NodeType("Math/Vector/Distance")
class Distance(
    type: String,
    @get:In ("p"      , Vec) @get:JsonProperty("in_p") val inP: Int,
    @get:In ("q"      , Vec) @get:JsonProperty("in_q") val inQ: Int,
    @get:Out("d(p, q)", Num ) @get:JsonProperty("out" ) val out: Int
) : Node(type) {
    override fun initialize(scope: Scope) {
        val inP = scope.dataPath(inP)
        val inQ = scope.dataPath(inQ)
        val out = scope.dataPath(out)

        out.set { vectorOp(inP.get(), inQ.get(), { p, q ->
            val pSubQ = p.sub(q)
            IntMath.sqrt(pSubQ.mul(pSubQ).reduceLanes(VectorOperators.ADD), RoundingMode.HALF_UP)
        }, { p, q ->
            val pSubQ = p.sub(q)
            sqrt(pSubQ.mul(pSubQ).reduceLanes(VectorOperators.ADD))
        }, { p, q ->
            val pSubQ = p.sub(q)
            sqrt(pSubQ.mul(pSubQ).reduceLanes(VectorOperators.ADD))
        }, scope.objectMapper) }
    }
}
