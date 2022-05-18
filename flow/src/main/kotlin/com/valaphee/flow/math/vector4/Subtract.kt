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

package com.valaphee.flow.math.vector4

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.DataPath
import com.valaphee.flow.DataPathException
import com.valaphee.flow.StatelessNode
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.Node
import com.valaphee.flow.spec.Out
import com.valaphee.flow.spec.Vec4
import com.valaphee.foundry.math.Double4
import com.valaphee.foundry.math.Float4
import com.valaphee.foundry.math.Int4

/**
 * @author Kevin Ludwig
 */
@Node("Math/Vector 4/Subtract")
class Subtract(
    @get:In ("A"    , Vec4, "") @get:JsonProperty("in_a") val inA: DataPath,
    @get:In ("B"    , Vec4, "") @get:JsonProperty("in_b") val inB: DataPath,
    @get:Out("A - B", Vec4    ) @get:JsonProperty("out" ) val out: DataPath
) : StatelessNode() {
    override fun initialize() {
        out.set {
            val inA = inA.get()
            val inB = inB.get()
            when (inA) {
                is Int4 -> when (inB) {
                    is Int4    -> inA             - inB
                    is Float4  -> inA.toFloat4()  - inB
                    is Double4 -> inA.toDouble4() - inB
                    else       -> DataPathException.invalidTypeInExpression("$inA - $inB")
                }
                is Float4 -> when (inB) {
                    is Int4    -> inA             - inB.toFloat4()
                    is Float4  -> inA             - inB
                    is Double4 -> inA.toDouble4() - inB
                    else      -> DataPathException.invalidTypeInExpression("$inA - $inB")
                }
                is Double4 -> when (inB) {
                    is Int4    -> inA - inB.toDouble4()
                    is Float4  -> inA - inB.toDouble4()
                    is Double4 -> inA - inB
                    else       -> DataPathException.invalidTypeInExpression("$inA - $inB")
                }
                else -> DataPathException.invalidTypeInExpression("$inA - $inB")
            }
        }
    }
}
