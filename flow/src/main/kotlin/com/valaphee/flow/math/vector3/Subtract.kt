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
@Node("Math/Vector 3/Subtract")
class Subtract(
    @get:In ("A"    , Vec3) @get:JsonProperty("in_a") val inA: DataPath,
    @get:In ("B"    , Vec3) @get:JsonProperty("in_b") val inB: DataPath,
    @get:Out("A - B", Vec3) @get:JsonProperty("out" ) val out: DataPath
) : StatelessNode() {
    override fun initialize() {
        out.set {
            val inA = inA.get()
            val inB = inB.get()
            when (inA) {
                is Int3 -> when (inB) {
                    is Int3    -> inA             - inB
                    is Float3  -> inA.toFloat3()  - inB
                    is Double3 -> inA.toDouble3() - inB
                    else       -> inA.toDouble3() - this.inB.getOrThrow("in_b")
                }
                is Float3 -> when (inB) {
                    is Int3    -> inA             - inB.toFloat3()
                    is Float3  -> inA             - inB
                    is Double3 -> inA.toDouble3() - inB
                    else       -> inA.toDouble3() - this.inB.getOrThrow("in_b")
                }
                is Double3 -> when (inB) {
                    is Int3    -> inA - inB.toDouble3()
                    is Float3  -> inA - inB.toDouble3()
                    is Double3 -> inA - inB
                    else       -> inA - this.inB.getOrThrow("in_b")
                }
                else -> this.inA.getOrThrow<Double3>("in_a") - this.inB.getOrThrow("in_b")
            }
        }
    }
}
