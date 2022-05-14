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
import com.valaphee.flow.DataPath
import com.valaphee.flow.DataPathException
import com.valaphee.flow.StatelessNode
import com.valaphee.flow.spec.DataType
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.Node
import com.valaphee.flow.spec.Out
import java.math.BigDecimal
import java.math.BigInteger

/**
 * @author Kevin Ludwig
 */
@Node("Math/Scalar/Multiply")
class Multiply(
    @get:In ("A"    , DataType.Num, "") @get:JsonProperty("in_a") val inA: DataPath,
    @get:In ("B"    , DataType.Num, "") @get:JsonProperty("in_b") val inB: DataPath,
    @get:Out("A × B", DataType.Num    ) @get:JsonProperty("out" ) val out: DataPath
) : StatelessNode() {
    override fun initialize() {
        out.set {
            val inA = inA.get()
            val inB = inB.get()
            when (inA) {
                is Byte -> when (inB) {
                    is Byte       -> inA                                * inB
                    is Short      -> inA                                * inB
                    is Int        -> inA                                * inB
                    is Long       -> inA                                * inB
                    is BigInteger -> BigInteger.valueOf(inA.toLong())   * inB
                    is Float      -> inA                                * inB
                    is Double     -> inA                                * inB
                    is BigDecimal -> BigDecimal.valueOf(inA.toDouble()) * inB
                    else          -> DataPathException.invalidTypeInExpression("$inA × $inB")
                }
                is Short -> when (inB) {
                    is Byte       -> inA                                * inB
                    is Short      -> inA                                * inB
                    is Int        -> inA                                * inB
                    is Long       -> inA                                * inB
                    is BigInteger -> BigInteger.valueOf(inA.toLong())   * inB
                    is Float      -> inA                                * inB
                    is Double     -> inA                                * inB
                    is BigDecimal -> BigDecimal.valueOf(inA.toDouble()) * inB
                    else          -> DataPathException.invalidTypeInExpression("$inA × $inB")
                }
                is Int -> when (inB) {
                    is Byte       -> inA                                * inB
                    is Short      -> inA                                * inB
                    is Int        -> inA                                * inB
                    is Long       -> inA                                * inB
                    is BigInteger -> BigInteger.valueOf(inA.toLong())   * inB
                    is Float      -> inA                                * inB
                    is Double     -> inA                                * inB
                    is BigDecimal -> BigDecimal.valueOf(inA.toDouble()) * inB
                    else          -> DataPathException.invalidTypeInExpression("$inA × $inB")
                }
                is Long -> when (inB) {
                    is Byte       -> inA                                * inB
                    is Short      -> inA                                * inB
                    is Int        -> inA                                * inB
                    is Long       -> inA                                * inB
                    is BigInteger -> BigInteger.valueOf(inA.toLong())   * inB
                    is Float      -> inA                                * inB
                    is Double     -> inA                                * inB
                    is BigDecimal -> BigDecimal.valueOf(inA.toDouble()) * inB
                    else          -> DataPathException.invalidTypeInExpression("$inA × $inB")
                }
                is BigInteger -> when (inB) {
                    is BigInteger -> inA                * inB
                    is BigDecimal -> inA.toBigDecimal() * inB
                    is Number     -> inA                * BigInteger.valueOf(inB.toLong())
                    else          -> DataPathException.invalidTypeInExpression("$inA × $inB")
                }
                is Float -> when (inB) {
                    is Byte       -> inA                                * inB
                    is Short      -> inA                                * inB
                    is Int        -> inA                                * inB
                    is Long       -> inA                                * inB
                    is BigInteger -> BigInteger.valueOf(inA.toLong())   * inB
                    is Float      -> inA                                * inB
                    is Double     -> inA                                * inB
                    is BigDecimal -> BigDecimal.valueOf(inA.toDouble()) * inB
                    else          -> DataPathException.invalidTypeInExpression("$inA × $inB")
                }
                is Double -> when (inB) {
                    is Byte       -> inA                                * inB
                    is Short      -> inA                                * inB
                    is Int        -> inA                                * inB
                    is Long       -> inA                                * inB
                    is BigInteger -> BigInteger.valueOf(inA.toLong())   * inB
                    is Float      -> inA                                * inB
                    is Double     -> inA                                * inB
                    is BigDecimal -> BigDecimal.valueOf(inA.toDouble()) * inB
                    else          -> DataPathException.invalidTypeInExpression("$inA × $inB")
                }
                is BigDecimal -> when (inB) {
                    is BigInteger -> inA * inB.toBigDecimal()
                    is BigDecimal -> inA * inB
                    is Number     -> inA * BigDecimal.valueOf(inB.toDouble())
                    else          -> DataPathException.invalidTypeInExpression("$inA × $inB")
                }
                else -> DataPathException.invalidTypeInExpression("$inA × $inB")
            }
        }
    }
}
