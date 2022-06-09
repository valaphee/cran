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

package com.valaphee.cran.node.math.scalar

import com.valaphee.cran.Virtual
import com.valaphee.cran.node.Node
import com.valaphee.cran.node.NodeVirtual
import com.valaphee.cran.path.DataPathException
import com.valaphee.cran.spec.NodeImpl
import java.math.BigDecimal
import java.math.BigInteger

/**
 * @author Kevin Ludwig
 */
@NodeImpl("virtual")
object SubtractVirtual : NodeVirtual {
    override fun initialize(node: Node, virtual: Virtual) = if (node is Subtract) {
        val inA = virtual.dataPath(node.inA)
        val inB = virtual.dataPath(node.inB)
        val out = virtual.dataPath(node.out)

        out.set {
            val _inA = inA.get()
            val _inB = inB.get()
            when (_inA) {
                is Byte -> when (_inB) {
                    is Byte       -> _inA                                - _inB
                    is Short      -> _inA                                - _inB
                    is Int        -> _inA                                - _inB
                    is Long       -> _inA                                - _inB
                    is BigInteger -> BigInteger.valueOf(_inA.toLong())   - _inB
                    is Float      -> _inA                                - _inB
                    is Double     -> _inA                                - _inB
                    is BigDecimal -> BigDecimal.valueOf(_inA.toDouble()) - _inB
                    else          -> throw DataPathException("$_inA - $_inB")
                }
                is Short -> when (_inB) {
                    is Byte       -> _inA                                - _inB
                    is Short      -> _inA                                - _inB
                    is Int        -> _inA                                - _inB
                    is Long       -> _inA                                - _inB
                    is BigInteger -> BigInteger.valueOf(_inA.toLong())   - _inB
                    is Float      -> _inA                                - _inB
                    is Double     -> _inA                                - _inB
                    is BigDecimal -> BigDecimal.valueOf(_inA.toDouble()) - _inB
                    else          -> throw DataPathException("$_inA - $_inB")
                }
                is Int -> when (_inB) {
                    is Byte       -> _inA                                - _inB
                    is Short      -> _inA                                - _inB
                    is Int        -> _inA                                - _inB
                    is Long       -> _inA                                - _inB
                    is BigInteger -> BigInteger.valueOf(_inA.toLong())   - _inB
                    is Float      -> _inA                                - _inB
                    is Double     -> _inA                                - _inB
                    is BigDecimal -> BigDecimal.valueOf(_inA.toDouble()) - _inB
                    else          -> throw DataPathException("$_inA - $_inB")
                }
                is Long -> when (_inB) {
                    is Byte       -> _inA                                - _inB
                    is Short      -> _inA                                - _inB
                    is Int        -> _inA                                - _inB
                    is Long       -> _inA                                - _inB
                    is BigInteger -> BigInteger.valueOf(_inA.toLong())   - _inB
                    is Float      -> _inA                                - _inB
                    is Double     -> _inA                                - _inB
                    is BigDecimal -> BigDecimal.valueOf(_inA.toDouble()) - _inB
                    else          -> throw DataPathException("$_inA - $_inB")
                }
                is BigInteger -> when (_inB) {
                    is BigInteger -> _inA                - _inB
                    is BigDecimal -> _inA.toBigDecimal() - _inB
                    is Number     -> _inA                - BigInteger.valueOf(_inB.toLong())
                    else          -> throw DataPathException("$_inA - $_inB")
                }
                is Float -> when (_inB) {
                    is Byte       -> _inA                                - _inB
                    is Short      -> _inA                                - _inB
                    is Int        -> _inA                                - _inB
                    is Long       -> _inA                                - _inB
                    is BigInteger -> BigInteger.valueOf(_inA.toLong())   - _inB
                    is Float      -> _inA                                - _inB
                    is Double     -> _inA                                - _inB
                    is BigDecimal -> BigDecimal.valueOf(_inA.toDouble()) - _inB
                    else          -> throw DataPathException("$_inA - $_inB")
                }
                is Double -> when (_inB) {
                    is Byte       -> _inA                                - _inB
                    is Short      -> _inA                                - _inB
                    is Int        -> _inA                                - _inB
                    is Long       -> _inA                                - _inB
                    is BigInteger -> BigInteger.valueOf(_inA.toLong())   - _inB
                    is Float      -> _inA                                - _inB
                    is Double     -> _inA                                - _inB
                    is BigDecimal -> BigDecimal.valueOf(_inA.toDouble()) - _inB
                    else          -> throw DataPathException("$_inA - $_inB")
                }
                is BigDecimal -> when (_inB) {
                    is BigInteger -> _inA - _inB.toBigDecimal()
                    is BigDecimal -> _inA - _inB
                    is Number     -> _inA - BigDecimal.valueOf(_inB.toDouble())
                    else          -> throw DataPathException("$_inA - $_inB")
                }
                else -> throw DataPathException("$_inA - $_inB")
            }
        }

        true
    } else false
}
