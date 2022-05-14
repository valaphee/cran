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

package com.valaphee.flow.math.scalar

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.common.math.IntMath
import com.valaphee.flow.DataPath
import com.valaphee.flow.DataPathException
import com.valaphee.flow.StatelessNode
import com.valaphee.flow.spec.DataType
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.Node
import com.valaphee.flow.spec.Out
import kotlin.math.pow

/**
 * @author Kevin Ludwig
 */
@Node("Math/Scalar/Exponentiate")
class Exponentiate(
    @get:In ("x" , DataType.Num, "") @get:JsonProperty("in_x") val inX: DataPath,
    @get:In ("n" , DataType.Num, "") @get:JsonProperty("in_n") val inN: DataPath,
    @get:Out("xⁿ", DataType.Num    ) @get:JsonProperty("out" ) val out: DataPath
) : StatelessNode() {
    override fun initialize() {
        out.set {
            val inX = inX.get()
            val inN = inN.get()
            when (inX) {
                is Float -> when (inN) {
                    is Float  -> inX.pow(inN)
                    is Double -> inX.toDouble().pow(inN)
                    is Number -> inX.pow(inN.toInt())
                    else      -> DataPathException.invalidTypeInExpression("$inX$inN")
                }
                is Double -> when (inN) {
                    is Float  -> inX.pow(inN.toDouble())
                    is Double -> inX.pow(inN)
                    is Number -> inX.pow(inN.toInt())
                    else      -> DataPathException.invalidTypeInExpression("$inX$inN")
                }
                is Number -> when (inN) {
                    is Float  -> inX.toFloat().pow(inN)
                    is Double -> inX.toDouble().pow(inN)
                    is Number -> IntMath.pow(inX.toInt(), inN.toInt())
                    else      -> DataPathException.invalidTypeInExpression("$inX$inN")
                }
                else -> DataPathException.invalidTypeInExpression("$inX$inN")
            }
        }
    }
}
