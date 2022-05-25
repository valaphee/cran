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
import com.valaphee.flow.DataPath
import com.valaphee.flow.Num
import com.valaphee.flow.StatelessNode
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.Node
import com.valaphee.flow.spec.Out
import com.valaphee.foundry.math.Double3
import com.valaphee.foundry.math.Float3
import com.valaphee.foundry.math.Int3

/**
 * @author Kevin Ludwig
 */
@Node("Math/Vector 3/Distance")
class Distance(
    @get:In ("p"      , Vec3) @get:JsonProperty("in_p") val inP: DataPath,
    @get:In ("q"      , Vec3) @get:JsonProperty("in_q") val inQ: DataPath,
    @get:Out("d(p, q)", Num ) @get:JsonProperty("out" ) val out: DataPath
) : StatelessNode() {
    override fun initialize() {
        out.set {
            val inP = inP.get()
            val inQ = inQ.get()
            when (inP) {
                is Int3 -> when (inQ) {
                    is Int3    -> inP.toFloat3() .distance(inQ.toFloat3()             )
                    is Float3  -> inP.toFloat3() .distance(inQ                        )
                    is Double3 -> inP.toDouble3().distance(inQ                        )
                    else       -> inP.toDouble3().distance(this.inQ.getOrThrow("in_p"))
                }
                is Float3 -> when (inQ) {
                    is Int3    -> inP            .distance(inQ.toFloat3()             )
                    is Float3  -> inP            .distance(inQ                        )
                    is Double3 -> inP.toDouble3().distance(inQ                        )
                    else       -> inP.toDouble3().distance(this.inQ.getOrThrow("in_p"))
                }
                is Double3 -> when (inQ) {
                    is Int3    -> inP.distance(inQ.toDouble3()            )
                    is Float3  -> inP.distance(inQ.toDouble3()            )
                    is Double3 -> inP.distance(inQ                        )
                    else       -> inP.distance(this.inQ.getOrThrow("in_p"))
                }
                else -> this.inP.getOrThrow<Double3>("in_p").distance(this.inQ.getOrThrow("in_q"))
            }
        }
    }
}
