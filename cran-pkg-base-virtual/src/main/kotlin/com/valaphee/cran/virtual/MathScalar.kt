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

package com.valaphee.cran.virtual

import com.google.common.math.IntMath
import com.valaphee.cran.Virtual
import com.valaphee.cran.node.BinaryOperation
import com.valaphee.cran.node.Node
import com.valaphee.cran.node.UnaryOperation
import com.valaphee.cran.node.math.scalar.Absolute
import com.valaphee.cran.node.math.scalar.Add
import com.valaphee.cran.node.math.scalar.Divide
import com.valaphee.cran.node.math.scalar.Exponentiate
import com.valaphee.cran.node.math.scalar.Modulo
import com.valaphee.cran.node.math.scalar.Multiply
import com.valaphee.cran.node.math.scalar.Subtract
import com.valaphee.cran.path.DataPathException
import com.valaphee.cran.spec.NodeImpl
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.absoluteValue
import kotlin.math.pow

/**
 * @author Kevin Ludwig
 */
@NodeImpl("virtual")
object MathScalar : Implementation {
    override fun initialize(node: Node, virtual: Virtual) = when (node) {
        is UnaryOperation -> {
            val `in` = virtual.dataPath(node.`in`)
            val out = virtual.dataPath(node.out)

            when (node) {
                is Absolute -> {
                    out.set {
                        val x = `in`.get()
                        when (x) {
                            is Byte       -> x.toInt().absoluteValue.toByte()
                            is Short      -> x.toInt().absoluteValue.toShort()
                            is Int        -> x        .absoluteValue
                            is Long       -> x        .absoluteValue
                            is BigInteger -> x        .abs()
                            is Float      -> x        .absoluteValue
                            is Double     -> x        .absoluteValue
                            is BigDecimal -> x        .abs()
                            else          -> throw DataPathException("|$x|")
                        }
                    }

                    true
                }
                else -> false
            }
        }
        is BinaryOperation -> {
            val in1 = virtual.dataPath(node.in1)
            val in2 = virtual.dataPath(node.in2)
            val out = virtual.dataPath(node.out)

            when (node) {
                is Add -> {
                    out.set {
                        val a = in1.get()
                        val b = in2.get()
                        when (a) {
                            is Byte -> when (b) {
                                is Byte       -> a                                + b
                                is Short      -> a                                + b
                                is Int        -> a                                + b
                                is Long       -> a                                + b
                                is BigInteger -> BigInteger.valueOf(a.toLong())   + b
                                is Float      -> a                                + b
                                is Double     -> a                                + b
                                is BigDecimal -> BigDecimal.valueOf(a.toDouble()) + b
                                else          -> throw DataPathException("$a + $b")
                            }
                            is Short -> when (b) {
                                is Byte       -> a                                + b
                                is Short      -> a                                + b
                                is Int        -> a                                + b
                                is Long       -> a                                + b
                                is BigInteger -> BigInteger.valueOf(a.toLong())   + b
                                is Float      -> a                                + b
                                is Double     -> a                                + b
                                is BigDecimal -> BigDecimal.valueOf(a.toDouble()) + b
                                else          -> throw DataPathException("$a + $b")
                            }
                            is Int -> when (b) {
                                is Byte       -> a                                + b
                                is Short      -> a                                + b
                                is Int        -> a                                + b
                                is Long       -> a                                + b
                                is BigInteger -> BigInteger.valueOf(a.toLong())   + b
                                is Float      -> a                                + b
                                is Double     -> a                                + b
                                is BigDecimal -> BigDecimal.valueOf(a.toDouble()) + b
                                else          -> throw DataPathException("$a + $b")
                            }
                            is Long -> when (b) {
                                is Byte       -> a                                + b
                                is Short      -> a                                + b
                                is Int        -> a                                + b
                                is Long       -> a                                + b
                                is BigInteger -> BigInteger.valueOf(a.toLong())   + b
                                is Float      -> a                                + b
                                is Double     -> a                                + b
                                is BigDecimal -> BigDecimal.valueOf(a.toDouble()) + b
                                else          -> throw DataPathException("$a + $b")
                            }
                            is BigInteger -> when (b) {
                                is BigInteger -> a                + b
                                is BigDecimal -> a.toBigDecimal() + b
                                is Number     -> a                + BigInteger.valueOf(b.toLong())
                                else          -> throw DataPathException("$a + $b")
                            }
                            is Float -> when (b) {
                                is Byte       -> a                                + b
                                is Short      -> a                                + b
                                is Int        -> a                                + b
                                is Long       -> a                                + b
                                is BigInteger -> BigInteger.valueOf(a.toLong())   + b
                                is Float      -> a                                + b
                                is Double     -> a                                + b
                                is BigDecimal -> BigDecimal.valueOf(a.toDouble()) + b
                                else          -> throw DataPathException("$a + $b")
                            }
                            is Double -> when (b) {
                                is Byte       -> a                                + b
                                is Short      -> a                                + b
                                is Int        -> a                                + b
                                is Long       -> a                                + b
                                is BigInteger -> BigInteger.valueOf(a.toLong())   + b
                                is Float      -> a                                + b
                                is Double     -> a                                + b
                                is BigDecimal -> BigDecimal.valueOf(a.toDouble()) + b
                                else          -> throw DataPathException("$a + $b")
                            }
                            is BigDecimal -> when (b) {
                                is BigInteger -> a + b.toBigDecimal()
                                is BigDecimal -> a + b
                                is Number     -> a + BigDecimal.valueOf(b.toDouble())
                                else          -> throw DataPathException("$a + $b")
                            }
                            else -> throw DataPathException("$a + $b")
                        }
                    }

                    true
                }
                is Subtract -> {
                    out.set {
                        val a = in1.get()
                        val b = in2.get()
                        when (a) {
                            is Byte -> when (b) {
                                is Byte       -> a                                - b
                                is Short      -> a                                - b
                                is Int        -> a                                - b
                                is Long       -> a                                - b
                                is BigInteger -> BigInteger.valueOf(a.toLong())   - b
                                is Float      -> a                                - b
                                is Double     -> a                                - b
                                is BigDecimal -> BigDecimal.valueOf(a.toDouble()) - b
                                else          -> throw DataPathException("$a - $b")
                            }
                            is Short -> when (b) {
                                is Byte       -> a                                - b
                                is Short      -> a                                - b
                                is Int        -> a                                - b
                                is Long       -> a                                - b
                                is BigInteger -> BigInteger.valueOf(a.toLong())   - b
                                is Float      -> a                                - b
                                is Double     -> a                                - b
                                is BigDecimal -> BigDecimal.valueOf(a.toDouble()) - b
                                else          -> throw DataPathException("$a - $b")
                            }
                            is Int -> when (b) {
                                is Byte       -> a                                - b
                                is Short      -> a                                - b
                                is Int        -> a                                - b
                                is Long       -> a                                - b
                                is BigInteger -> BigInteger.valueOf(a.toLong())   - b
                                is Float      -> a                                - b
                                is Double     -> a                                - b
                                is BigDecimal -> BigDecimal.valueOf(a.toDouble()) - b
                                else          -> throw DataPathException("$a - $b")
                            }
                            is Long -> when (b) {
                                is Byte       -> a                                - b
                                is Short      -> a                                - b
                                is Int        -> a                                - b
                                is Long       -> a                                - b
                                is BigInteger -> BigInteger.valueOf(a.toLong())   - b
                                is Float      -> a                                - b
                                is Double     -> a                                - b
                                is BigDecimal -> BigDecimal.valueOf(a.toDouble()) - b
                                else          -> throw DataPathException("$a - $b")
                            }
                            is BigInteger -> when (b) {
                                is BigInteger -> a                - b
                                is BigDecimal -> a.toBigDecimal() - b
                                is Number     -> a                - BigInteger.valueOf(b.toLong())
                                else          -> throw DataPathException("$a - $b")
                            }
                            is Float -> when (b) {
                                is Byte       -> a                                - b
                                is Short      -> a                                - b
                                is Int        -> a                                - b
                                is Long       -> a                                - b
                                is BigInteger -> BigInteger.valueOf(a.toLong())   - b
                                is Float      -> a                                - b
                                is Double     -> a                                - b
                                is BigDecimal -> BigDecimal.valueOf(a.toDouble()) - b
                                else          -> throw DataPathException("$a - $b")
                            }
                            is Double -> when (b) {
                                is Byte       -> a                                - b
                                is Short      -> a                                - b
                                is Int        -> a                                - b
                                is Long       -> a                                - b
                                is BigInteger -> BigInteger.valueOf(a.toLong())   - b
                                is Float      -> a                                - b
                                is Double     -> a                                - b
                                is BigDecimal -> BigDecimal.valueOf(a.toDouble()) - b
                                else          -> throw DataPathException("$a - $b")
                            }
                            is BigDecimal -> when (b) {
                                is BigInteger -> a - b.toBigDecimal()
                                is BigDecimal -> a - b
                                is Number     -> a - BigDecimal.valueOf(b.toDouble())
                                else          -> throw DataPathException("$a - $b")
                            }
                            else -> throw DataPathException("$a - $b")
                        }
                    }

                    true
                }
                is Multiply -> {
                    out.set {
                        val a = in1.get()
                        val b = in2.get()
                        when (a) {
                            is Byte -> when (b) {
                                is Byte       -> a                                * b
                                is Short      -> a                                * b
                                is Int        -> a                                * b
                                is Long       -> a                                * b
                                is BigInteger -> BigInteger.valueOf(a.toLong())   * b
                                is Float      -> a                                * b
                                is Double     -> a                                * b
                                is BigDecimal -> BigDecimal.valueOf(a.toDouble()) * b
                                else          -> throw DataPathException("$a × $b")
                            }
                            is Short -> when (b) {
                                is Byte       -> a                                * b
                                is Short      -> a                                * b
                                is Int        -> a                                * b
                                is Long       -> a                                * b
                                is BigInteger -> BigInteger.valueOf(a.toLong())   * b
                                is Float      -> a                                * b
                                is Double     -> a                                * b
                                is BigDecimal -> BigDecimal.valueOf(a.toDouble()) * b
                                else          -> throw DataPathException("$a × $b")
                            }
                            is Int -> when (b) {
                                is Byte       -> a                                * b
                                is Short      -> a                                * b
                                is Int        -> a                                * b
                                is Long       -> a                                * b
                                is BigInteger -> BigInteger.valueOf(a.toLong())   * b
                                is Float      -> a                                * b
                                is Double     -> a                                * b
                                is BigDecimal -> BigDecimal.valueOf(a.toDouble()) * b
                                else          -> throw DataPathException("$a × $b")
                            }
                            is Long -> when (b) {
                                is Byte       -> a                                * b
                                is Short      -> a                                * b
                                is Int        -> a                                * b
                                is Long       -> a                                * b
                                is BigInteger -> BigInteger.valueOf(a.toLong())   * b
                                is Float      -> a                                * b
                                is Double     -> a                                * b
                                is BigDecimal -> BigDecimal.valueOf(a.toDouble()) * b
                                else          -> throw DataPathException("$a × $b")
                            }
                            is BigInteger -> when (b) {
                                is BigInteger -> a                * b
                                is BigDecimal -> a.toBigDecimal() * b
                                is Number     -> a                * BigInteger.valueOf(b.toLong())
                                else          -> throw DataPathException("$a × $b")
                            }
                            is Float -> when (b) {
                                is Byte       -> a                                * b
                                is Short      -> a                                * b
                                is Int        -> a                                * b
                                is Long       -> a                                * b
                                is BigInteger -> BigInteger.valueOf(a.toLong())   * b
                                is Float      -> a                                * b
                                is Double     -> a                                * b
                                is BigDecimal -> BigDecimal.valueOf(a.toDouble()) * b
                                else          -> throw DataPathException("$a × $b")
                            }
                            is Double -> when (b) {
                                is Byte       -> a                                * b
                                is Short      -> a                                * b
                                is Int        -> a                                * b
                                is Long       -> a                                * b
                                is BigInteger -> BigInteger.valueOf(a.toLong())   * b
                                is Float      -> a                                * b
                                is Double     -> a                                * b
                                is BigDecimal -> BigDecimal.valueOf(a.toDouble()) * b
                                else          -> throw DataPathException("$a × $b")
                            }
                            is BigDecimal -> when (b) {
                                is BigInteger -> a * b.toBigDecimal()
                                is BigDecimal -> a * b
                                is Number     -> a * BigDecimal.valueOf(b.toDouble())
                                else          -> throw DataPathException("$a × $b")
                            }
                            else -> throw DataPathException("$a × $b")
                        }
                    }

                    true
                }
                is Divide -> {
                    out.set {
                        val a = in1.get()
                        val b = in2.get()
                        when (a) {
                            is Byte -> when (b) {
                                is Byte       -> a                                / b
                                is Short      -> a                                / b
                                is Int        -> a                                / b
                                is Long       -> a                                / b
                                is BigInteger -> BigInteger.valueOf(a.toLong())   / b
                                is Float      -> a                                / b
                                is Double     -> a                                / b
                                is BigDecimal -> BigDecimal.valueOf(a.toDouble()) / b
                                else          -> throw DataPathException("$a ÷ $b")
                            }
                            is Short -> when (b) {
                                is Byte       -> a                                / b
                                is Short      -> a                                / b
                                is Int        -> a                                / b
                                is Long       -> a                                / b
                                is BigInteger -> BigInteger.valueOf(a.toLong())   / b
                                is Float      -> a                                / b
                                is Double     -> a                                / b
                                is BigDecimal -> BigDecimal.valueOf(a.toDouble()) / b
                                else          -> throw DataPathException("$a ÷ $b")
                            }
                            is Int -> when (b) {
                                is Byte       -> a                                / b
                                is Short      -> a                                / b
                                is Int        -> a                                / b
                                is Long       -> a                                / b
                                is BigInteger -> BigInteger.valueOf(a.toLong())   / b
                                is Float      -> a                                / b
                                is Double     -> a                                / b
                                is BigDecimal -> BigDecimal.valueOf(a.toDouble()) / b
                                else          -> throw DataPathException("$a ÷ $b")
                            }
                            is Long -> when (b) {
                                is Byte       -> a                                / b
                                is Short      -> a                                / b
                                is Int        -> a                                / b
                                is Long       -> a                                / b
                                is BigInteger -> BigInteger.valueOf(a.toLong())   / b
                                is Float      -> a                                / b
                                is Double     -> a                                / b
                                is BigDecimal -> BigDecimal.valueOf(a.toDouble()) / b
                                else          -> throw DataPathException("$a ÷ $b")
                            }
                            is BigInteger -> when (b) {
                                is BigInteger -> a                / b
                                is BigDecimal -> a.toBigDecimal() / b
                                is Number     -> a                / BigInteger.valueOf(b.toLong())
                                else          -> throw DataPathException("$a ÷ $b")
                            }
                            is Float -> when (b) {
                                is Byte       -> a                                / b
                                is Short      -> a                                / b
                                is Int        -> a                                / b
                                is Long       -> a                                / b
                                is BigInteger -> BigInteger.valueOf(a.toLong())   / b
                                is Float      -> a                                / b
                                is Double     -> a                                / b
                                is BigDecimal -> BigDecimal.valueOf(a.toDouble()) / b
                                else          -> throw DataPathException("$a ÷ $b")
                            }
                            is Double -> when (b) {
                                is Byte       -> a                                / b
                                is Short      -> a                                / b
                                is Int        -> a                                / b
                                is Long       -> a                                / b
                                is BigInteger -> BigInteger.valueOf(a.toLong())   / b
                                is Float      -> a                                / b
                                is Double     -> a                                / b
                                is BigDecimal -> BigDecimal.valueOf(a.toDouble()) / b
                                else          -> throw DataPathException("$a ÷ $b")
                            }
                            is BigDecimal -> when (b) {
                                is BigInteger -> a / b.toBigDecimal()
                                is BigDecimal -> a / b
                                is Number     -> a / BigDecimal.valueOf(b.toDouble())
                                else          -> throw DataPathException("$a ÷ $b")
                            }
                            else -> throw DataPathException("$a ÷ $b")
                        }
                    }

                    true
                }
                is Modulo -> {
                    out.set {
                        val a = in1.get()
                        val b = in2.get()
                        when (a) {
                            is Byte -> when (b) {
                                is Byte       -> a                                % b
                                is Short      -> a                                % b
                                is Int        -> a                                % b
                                is Long       -> a                                % b
                                is BigInteger -> BigInteger.valueOf(a.toLong())   % b
                                is Float      -> a                                % b
                                is Double     -> a                                % b
                                is BigDecimal -> BigDecimal.valueOf(a.toDouble()) % b
                                else          -> throw DataPathException("$a mod $b")
                            }
                            is Short -> when (b) {
                                is Byte       -> a                                % b
                                is Short      -> a                                % b
                                is Int        -> a                                % b
                                is Long       -> a                                % b
                                is BigInteger -> BigInteger.valueOf(a.toLong())   % b
                                is Float      -> a                                % b
                                is Double     -> a                                % b
                                is BigDecimal -> BigDecimal.valueOf(a.toDouble()) % b
                                else          -> throw DataPathException("$a mod $b")
                            }
                            is Int -> when (b) {
                                is Byte       -> a                                % b
                                is Short      -> a                                % b
                                is Int        -> a                                % b
                                is Long       -> a                                % b
                                is BigInteger -> BigInteger.valueOf(a.toLong())   % b
                                is Float      -> a                                % b
                                is Double     -> a                                % b
                                is BigDecimal -> BigDecimal.valueOf(a.toDouble()) % b
                                else          -> throw DataPathException("$a mod $b")
                            }
                            is Long -> when (b) {
                                is Byte       -> a                                % b
                                is Short      -> a                                % b
                                is Int        -> a                                % b
                                is Long       -> a                                % b
                                is BigInteger -> BigInteger.valueOf(a.toLong())   % b
                                is Float      -> a                                % b
                                is Double     -> a                                % b
                                is BigDecimal -> BigDecimal.valueOf(a.toDouble()) % b
                                else          -> throw DataPathException("$a mod $b")
                            }
                            is BigInteger -> when (b) {
                                is BigInteger -> a                % b
                                is BigDecimal -> a.toBigDecimal() % b
                                is Number     -> a                % BigInteger.valueOf(b.toLong())
                                else          -> throw DataPathException("$a mod $b")
                            }
                            is Float -> when (b) {
                                is Byte       -> a                                % b
                                is Short      -> a                                % b
                                is Int        -> a                                % b
                                is Long       -> a                                % b
                                is BigInteger -> BigInteger.valueOf(a.toLong())   % b
                                is Float      -> a                                % b
                                is Double     -> a                                % b
                                is BigDecimal -> BigDecimal.valueOf(a.toDouble()) % b
                                else          -> throw DataPathException("$a mod $b")
                            }
                            is Double -> when (b) {
                                is Byte       -> a                                % b
                                is Short      -> a                                % b
                                is Int        -> a                                % b
                                is Long       -> a                                % b
                                is BigInteger -> BigInteger.valueOf(a.toLong())   % b
                                is Float      -> a                                % b
                                is Double     -> a                                % b
                                is BigDecimal -> BigDecimal.valueOf(a.toDouble()) % b
                                else          -> throw DataPathException("$a mod $b")
                            }
                            is BigDecimal -> when (b) {
                                is BigInteger -> a % b.toBigDecimal()
                                is BigDecimal -> a % b
                                is Number     -> a % BigDecimal.valueOf(b.toDouble())
                                else          -> throw DataPathException("$a mod $b")
                            }
                            else -> throw DataPathException("$a mod $b")
                        }
                    }

                    true
                }
                is Exponentiate -> {
                    out.set {
                        val x = in1.get()
                        val n = in2.get()
                        when (x) {
                            is Float -> when (n) {
                                is Float  -> x           .pow(n        )
                                is Double -> x.toDouble().pow(n        )
                                is Number -> x           .pow(n.toInt())
                                else      -> throw DataPathException("$x$n")
                            }
                            is Double -> when (n) {
                                is Float  -> x.pow(n.toDouble())
                                is Double -> x.pow(n           )
                                is Number -> x.pow(n.toInt()   )
                                else      -> throw DataPathException("$x$n")
                            }
                            is Number -> when (n) {
                                is Float  -> x.toFloat() .pow(n)
                                is Double -> x.toDouble().pow(n)
                                is Number -> IntMath.pow(x.toInt(), n.toInt())
                                else      -> throw DataPathException("$x$n")
                            }
                            else -> throw DataPathException("$x$n")
                        }
                    }

                    true
                }
                else -> false
            }
        }
        else -> false
    }
}
