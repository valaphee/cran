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
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.NodeType
import com.valaphee.flow.spec.Out
import com.valaphee.foundry.math.Double3
import com.valaphee.foundry.math.Float3
import com.valaphee.foundry.math.Int3
import kotlin.math.absoluteValue

/**
 * @author Kevin Ludwig
 */
@NodeType("Math/Vector 3/Absolute")
class Absolute(
    @get:In ("X"  , Vec3) @get:JsonProperty("in" ) val `in`: Int,
    @get:Out("|X|", Vec3) @get:JsonProperty("out") val out : Int
) : Node() {
    override fun initialize(scope: Scope) {
        val `in` = scope.dataPath(`in`)
        val out = scope.dataPath(out)

        out.set {
            val _in = `in`.get()
            when (_in) {
                is Int3    -> _in                           .abs()
                is Float3  -> _in                           .abs()
                is Double3 -> _in                           .abs()
                else       -> `in`.getOrThrow<Double3>("in").abs()
            }
        }
    }

    companion object {
        private fun Int3.abs() = Int3(x.absoluteValue, y.absoluteValue, z.absoluteValue)

        private fun Float3.abs() = Float3(x.absoluteValue, y.absoluteValue, z.absoluteValue)

        private fun Double3.abs() = Double3(x.absoluteValue, y.absoluteValue, z.absoluteValue)
    }
}
