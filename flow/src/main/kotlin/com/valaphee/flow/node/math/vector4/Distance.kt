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
import com.valaphee.flow.node.Num
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.NodeType
import com.valaphee.flow.spec.Out
import com.valaphee.foundry.math.Double4
import com.valaphee.foundry.math.Float4
import com.valaphee.foundry.math.Int4

/**
 * @author Kevin Ludwig
 */
@NodeType("Math/Vector 4/Distance")
class Distance(
    type: String,
    @get:In ("p"      , Vec4) @get:JsonProperty("in_p") val inP: Int,
    @get:In ("q"      , Vec4) @get:JsonProperty("in_q") val inQ: Int,
    @get:Out("d(p, q)", Num ) @get:JsonProperty("out" ) val out: Int
) : Node(type) {
    override fun initialize(scope: Scope) {
        val inP = scope.dataPath(inP)
        val inQ = scope.dataPath(inQ)
        val out = scope.dataPath(out)

        out.set {
            val _inP = inP.get()
            val _inQ = inQ.get()
            when (_inP) {
                is Int4 -> when (_inQ) {
                    is Int4    -> _inP.toFloat4() .distance(_inQ.toFloat4()       )
                    is Float4  -> _inP.toFloat4() .distance(_inQ                  )
                    is Double4 -> _inP.toDouble4().distance(_inQ                  )
                    else       -> _inP.toDouble4().distance(inQ.getOrThrow("in_p"))
                }
                is Float4 -> when (_inQ) {
                    is Int4    -> _inP            .distance(_inQ.toFloat4()       )
                    is Float4  -> _inP            .distance(_inQ                  )
                    is Double4 -> _inP.toDouble4().distance(_inQ                  )
                    else       -> _inP.toDouble4().distance(inQ.getOrThrow("in_p"))
                }
                is Double4 -> when (_inQ) {
                    is Int4    -> _inP.distance(_inQ.toDouble4()      )
                    is Float4  -> _inP.distance(_inQ.toDouble4()      )
                    is Double4 -> _inP.distance(_inQ                  )
                    else       -> _inP.distance(inQ.getOrThrow("in_p"))
                }
                else -> inP.getOrThrow<Double4>("in_p").distance(inQ.getOrThrow("in_q"))
            }
        }
    }
}
