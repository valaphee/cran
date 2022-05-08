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

package com.valaphee.flow.math

import com.valaphee.flow.DataPath
import kotlinx.coroutines.CoroutineScope
import java.math.BigDecimal
import java.math.BigInteger

/**
 * @author Kevin Ludwig
 */
class Sub(
    override val inA: DataPath,
    override val inB: DataPath,
    override val out: DataPath
) : MathNode() {
    override fun run(scope: CoroutineScope) {
        out.set {
            val inA = inA.get()
            val inB = inB.get()
            when (inA) {
                is Byte -> when (inB) {
                    is Byte -> inA - inB
                    is Short -> inA - inB
                    is Int -> inA - inB
                    is Long -> inA - inB
                    is Float -> inA - inB
                    is Double -> inA - inB
                    else -> error("$inA - $inB")
                }
                is Short -> when (inB) {
                    is Byte -> inA - inB
                    is Short -> inA - inB
                    is Int -> inA - inB
                    is Long -> inA - inB
                    is Float -> inA - inB
                    is Double -> inA - inB
                    else -> error("$inA - $inB")
                }
                is Int -> when (inB) {
                    is Byte -> inA - inB
                    is Short -> inA - inB
                    is Int -> inA - inB
                    is Long -> inA - inB
                    is Float -> inA - inB
                    is Double -> inA - inB
                    else -> error("$inA - $inB")
                }
                is Long -> when (inB) {
                    is Byte -> inA - inB
                    is Short -> inA - inB
                    is Int -> inA - inB
                    is Long -> inA - inB
                    is Float -> inA - inB
                    is Double -> inA - inB
                    else -> error("$inA - $inB")
                }
                is BigInteger -> when (inB) {
                    is BigInteger -> inA - inB
                    is BigDecimal -> inA.toBigDecimal() - inB
                    is Number -> inA - BigInteger.valueOf(inB.toLong())
                    else -> error("$inA - $inB")
                }
                is Float -> when (inB) {
                    is Byte -> inA - inB
                    is Short -> inA - inB
                    is Int -> inA - inB
                    is Long -> inA - inB
                    is Float -> inA - inB
                    is Double -> inA - inB
                    else -> error("$inA - $inB")
                }
                is Double -> when (inB) {
                    is Byte -> inA - inB
                    is Short -> inA - inB
                    is Int -> inA - inB
                    is Long -> inA - inB
                    is Float -> inA - inB
                    is Double -> inA - inB
                    else -> error("$inA - $inB")
                }
                is BigDecimal -> when (inB) {
                    is BigInteger -> inA - inB.toBigDecimal()
                    is BigDecimal -> inA - inB
                    is Number -> inA - BigDecimal.valueOf(inB.toDouble())
                    else -> error("$inA - $inB")
                }
                else -> error("$inA - $inB")
            }
        }
    }
}
