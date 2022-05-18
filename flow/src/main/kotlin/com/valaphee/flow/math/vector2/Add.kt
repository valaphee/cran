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
import com.valaphee.flow.DataPath
import com.valaphee.flow.DataPathException
import com.valaphee.flow.StatelessNode
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.Node
import com.valaphee.flow.spec.Out
import com.valaphee.flow.spec.Vec2
import com.valaphee.foundry.math.Double2
import com.valaphee.foundry.math.Float2
import com.valaphee.foundry.math.Int2

/**
 * @author Kevin Ludwig
 */
@Node("Math/Vector 2/Add")
class Add(
    @get:In ("A"    , Vec2, "") @get:JsonProperty("in_a") val inA: DataPath,
    @get:In ("B"    , Vec2, "") @get:JsonProperty("in_b") val inB: DataPath,
    @get:Out("A + B", Vec2    ) @get:JsonProperty("out" ) val out: DataPath
) : StatelessNode() {
    override fun initialize() {
        out.set {
            val inA = inA.get()
            val inB = inB.get()
            when (inA) {
                is Int2 -> when (inB) {
                    is Int2    -> inA             + inB
                    is Float2  -> inA.toFloat2()  + inB
                    is Double2 -> inA.toDouble2() + inB
                    else       -> DataPathException.invalidTypeInExpression("$inA + $inB")
                }
                is Float2 -> when (inB) {
                    is Int2    -> inA             + inB.toFloat2()
                    is Float2  -> inA             + inB
                    is Double2 -> inA.toDouble2() + inB
                    else      -> DataPathException.invalidTypeInExpression("$inA + $inB")
                }
                is Double2 -> when (inB) {
                    is Int2    -> inA + inB.toDouble2()
                    is Float2  -> inA + inB.toDouble2()
                    is Double2 -> inA + inB
                    else       -> DataPathException.invalidTypeInExpression("$inA + $inB")
                }
                else -> DataPathException.invalidTypeInExpression("$inA + $inB")
            }
        }
    }
}
