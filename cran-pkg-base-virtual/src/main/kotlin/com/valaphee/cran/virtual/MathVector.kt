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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.valaphee.cran.Virtual
import com.valaphee.cran.node.BinaryOperation
import com.valaphee.cran.node.Node
import com.valaphee.cran.node.UnaryOperation
import com.valaphee.cran.node.math.vector.Absolute
import com.valaphee.cran.node.math.vector.Add
import com.valaphee.cran.node.math.vector.Distance
import com.valaphee.cran.node.math.vector.Subtract
import com.valaphee.cran.node.math.vector.doubleVectorSpeciesByLength
import com.valaphee.cran.spec.NodeImpl
import jdk.incubator.vector.DoubleVector
import jdk.incubator.vector.FloatVector
import jdk.incubator.vector.IntVector
import jdk.incubator.vector.VectorOperators
import kotlin.math.sqrt

/**
 * @author Kevin Ludwig
 */
@NodeImpl("virtual")
object MathVector : Implementation {
    override fun initialize(node: Node, virtual: Virtual) = when (node) {
        is UnaryOperation -> {
            val `in` = virtual.dataPath(node.`in`)
            val out = virtual.dataPath(node.out)

            when (node) {
                is Absolute -> {
                    out.set { unaryOp(`in`.get(), { it.abs() }, { it.abs() }, { it.abs() }, virtual.objectMapper) }

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
                    out.set { binaryOp(in1.get(), in2.get(), { a, b -> a.add(b) }, { a, b -> a.add(b) }, { a, b -> a.add(b) }, virtual.objectMapper) }

                    true
                }
                is Subtract -> {
                    out.set { binaryOp(in1.get(), in2.get(), { a, b -> a.sub(b) }, { a, b -> a.sub(b) }, { a, b -> a.sub(b) }, virtual.objectMapper) }

                    true
                }
                is Distance -> {
                    out.set {
                        binaryOp(in1.get(), in2.get(), { p, q ->
                            val pSubQ = p.sub(q)
                            sqrt(pSubQ.mul(pSubQ).reduceLanes(VectorOperators.ADD).toFloat()).toInt()
                        }, { p, q ->
                            val pSubQ = p.sub(q)
                            sqrt(pSubQ.mul(pSubQ).reduceLanes(VectorOperators.ADD))
                        }, { p, q ->
                            val pSubQ = p.sub(q)
                            sqrt(pSubQ.mul(pSubQ).reduceLanes(VectorOperators.ADD))
                        }, virtual.objectMapper)
                    }

                    true
                }
                else -> false
            }
        }
        else -> false
    }

    private inline fun unaryOp(`in`: Any?, intOp: (IntVector) -> Any, floatOp: (FloatVector) -> Any, doubleOp: (DoubleVector) -> Any, objectMapper: ObjectMapper) = when (`in`) {
        is IntVector    -> intOp   (`in`                                         )
        is FloatVector  -> floatOp (`in`                                         )
        is DoubleVector -> doubleOp(`in`                                         )
        else            -> doubleOp(objectMapper.convertValue(checkNotNull(`in`)))
    }

    private inline fun binaryOp(in1: Any?, in2: Any?, intOp: (IntVector, IntVector) -> Any, floatOp: (FloatVector, FloatVector) -> Any, doubleOp: (DoubleVector, DoubleVector) -> Any, objectMapper: ObjectMapper) = when (in1) {
        is IntVector -> when (in2) {
            is IntVector    -> intOp   (in1                                                                                                       , in2                                         )
            is FloatVector  -> floatOp (in1.convert     (VectorOperators.I2F,                                            0).reinterpretAsFloats() , in2                                         )
            is DoubleVector -> doubleOp(in1.convertShape(VectorOperators.I2D, doubleVectorSpeciesByLength(in1.length()), 0).reinterpretAsDoubles(), in2                                         )
            else            -> doubleOp(in1.convertShape(VectorOperators.I2D, doubleVectorSpeciesByLength(in1.length()), 0).reinterpretAsDoubles(), objectMapper.convertValue(checkNotNull(in2)))
        }
        is FloatVector -> when (in2) {
            is IntVector    -> floatOp (in1                                                                                                       , in2.convert(VectorOperators.I2F, 0).reinterpretAsFloats())
            is FloatVector  -> floatOp (in1                                                                                                       , in2                                                      )
            is DoubleVector -> doubleOp(in1.convertShape(VectorOperators.F2D, doubleVectorSpeciesByLength(in1.length()), 0).reinterpretAsDoubles(), in2                                                      )
            else            -> doubleOp(in1.convertShape(VectorOperators.F2D, doubleVectorSpeciesByLength(in1.length()), 0).reinterpretAsDoubles(), objectMapper.convertValue(checkNotNull(in2))             )
        }
        is DoubleVector -> when (in2) {
            is IntVector    -> doubleOp(in1, in2.convertShape(VectorOperators.I2D, doubleVectorSpeciesByLength(in2.length()), 0).reinterpretAsDoubles())
            is FloatVector  -> doubleOp(in1, in2.convertShape(VectorOperators.F2D, doubleVectorSpeciesByLength(in2.length()), 0).reinterpretAsDoubles())
            is DoubleVector -> doubleOp(in1, in2                                                                                                       )
            else            -> doubleOp(in1, objectMapper.convertValue(checkNotNull(in2))                                                              )
        }
        else -> doubleOp(objectMapper.convertValue(checkNotNull(in1)), when (in2) {
            is IntVector    -> in2.convertShape(VectorOperators.I2D, doubleVectorSpeciesByLength(in2.length()), 0).reinterpretAsDoubles()
            is FloatVector  -> in2.convertShape(VectorOperators.F2D, doubleVectorSpeciesByLength(in2.length()), 0).reinterpretAsDoubles()
            is DoubleVector -> in2
            else            -> objectMapper.convertValue(checkNotNull(in2))
        })
    }
}
