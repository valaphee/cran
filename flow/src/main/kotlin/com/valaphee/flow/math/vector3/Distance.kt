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

package com.valaphee.flow.math.vector3

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.Scope
import com.valaphee.flow.Node
import com.valaphee.flow.Num
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.NodeType
import com.valaphee.flow.spec.Out
import com.valaphee.foundry.math.Double3
import com.valaphee.foundry.math.Float3
import com.valaphee.foundry.math.Int3

/**
 * @author Kevin Ludwig
 */
@NodeType("Math/Vector 3/Distance")
class Distance(
    @get:In ("p"      , Vec3) @get:JsonProperty("in_p") val inP: Int,
    @get:In ("q"      , Vec3) @get:JsonProperty("in_q") val inQ: Int,
    @get:Out("d(p, q)", Num ) @get:JsonProperty("out" ) val out: Int
) : Node() {
    override fun initialize(scope: Scope) {
        val inP = scope.dataPath(inP)
        val inQ = scope.dataPath(inQ)
        val out = scope.dataPath(out)

        out.set {
            val _inP = inP.get()
            val _inQ = inQ.get()
            when (_inP) {
                is Int3 -> when (_inQ) {
                    is Int3    -> _inP.toFloat3() .distance(_inQ.toFloat3()       )
                    is Float3  -> _inP.toFloat3() .distance(_inQ                  )
                    is Double3 -> _inP.toDouble3().distance(_inQ                  )
                    else       -> _inP.toDouble3().distance(inQ.getOrThrow("in_p"))
                }
                is Float3 -> when (_inQ) {
                    is Int3    -> _inP            .distance(_inQ.toFloat3()       )
                    is Float3  -> _inP            .distance(_inQ                  )
                    is Double3 -> _inP.toDouble3().distance(_inQ                  )
                    else       -> _inP.toDouble3().distance(inQ.getOrThrow("in_p"))
                }
                is Double3 -> when (_inQ) {
                    is Int3    -> _inP.distance(_inQ.toDouble3()      )
                    is Float3  -> _inP.distance(_inQ.toDouble3()      )
                    is Double3 -> _inP.distance(_inQ                  )
                    else       -> _inP.distance(inQ.getOrThrow("in_p"))
                }
                else -> inP.getOrThrow<Double3>("in_p").distance(inQ.getOrThrow("in_q"))
            }
        }
    }
}
