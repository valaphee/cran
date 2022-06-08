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

package com.valaphee.cran.node.math.vector

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import jdk.incubator.vector.DoubleVector
import jdk.incubator.vector.FloatVector
import jdk.incubator.vector.IntVector
import jdk.incubator.vector.VectorOperators

const val Vec  = """{"type":"array","items":{"type":"number"}}"""
const val Vec2 = """{"type":"array","items":{"type":"number"},"minItems":2,"maxItems":2}"""
const val Vec3 = """{"type":"array","items":{"type":"number"},"minItems":3,"maxItems":3}"""
const val Vec4 = """{"type":"array","items":{"type":"number"},"minItems":4,"maxItems":4}"""

inline fun vectorOp(x: Any?, intOp: (IntVector) -> Any, floatOp: (FloatVector) -> Any, doubleOp: (DoubleVector) -> Any, objectMapper: ObjectMapper) = when (x) {
    is IntVector    -> intOp   (x                                         )
    is FloatVector  -> floatOp (x                                         )
    is DoubleVector -> doubleOp(x                                         )
    else            -> doubleOp(objectMapper.convertValue(checkNotNull(x)))
}

inline fun vectorOp(a: Any?, b: Any?, intOp: (IntVector, IntVector) -> Any, floatOp: (FloatVector, FloatVector) -> Any, doubleOp: (DoubleVector, DoubleVector) -> Any, objectMapper: ObjectMapper) = when (a) {
    is IntVector -> when (b) {
        is IntVector    -> intOp   (a                                                                                                     , b                                         )
        is FloatVector  -> floatOp (a.convert     (VectorOperators.I2F,                                          0).reinterpretAsFloats() , b                                         )
        is DoubleVector -> doubleOp(a.convertShape(VectorOperators.I2D, doubleVectorSpeciesByLength(a.length()), 0).reinterpretAsDoubles(), b                                         )
        else            -> doubleOp(a.convertShape(VectorOperators.I2D, doubleVectorSpeciesByLength(a.length()), 0).reinterpretAsDoubles(), objectMapper.convertValue(checkNotNull(b)))
    }
    is FloatVector -> when (b) {
        is IntVector    -> floatOp (a                                                                                                     , b.convert(VectorOperators.I2F, 0).reinterpretAsFloats())
        is FloatVector  -> floatOp (a                                                                                                     , b                                                      )
        is DoubleVector -> doubleOp(a.convertShape(VectorOperators.F2D, doubleVectorSpeciesByLength(a.length()), 0).reinterpretAsDoubles(), b                                                      )
        else            -> doubleOp(a.convertShape(VectorOperators.F2D, doubleVectorSpeciesByLength(a.length()), 0).reinterpretAsDoubles(), objectMapper.convertValue(checkNotNull(b))             )
    }
    is DoubleVector -> when (b) {
        is IntVector    -> doubleOp(a, b.convertShape(VectorOperators.I2D, doubleVectorSpeciesByLength(b.length()), 0).reinterpretAsDoubles())
        is FloatVector  -> doubleOp(a, b.convertShape(VectorOperators.F2D, doubleVectorSpeciesByLength(b.length()), 0).reinterpretAsDoubles())
        is DoubleVector -> doubleOp(a, b                                                                                                     )
        else            -> doubleOp(a, objectMapper.convertValue(checkNotNull(b))                                                            )
    }
    else -> doubleOp(objectMapper.convertValue(checkNotNull(a)), when (b) {
        is IntVector    -> b.convertShape(VectorOperators.I2D, doubleVectorSpeciesByLength(b.length()), 0).reinterpretAsDoubles()
        is FloatVector  -> b.convertShape(VectorOperators.F2D, doubleVectorSpeciesByLength(b.length()), 0).reinterpretAsDoubles()
        is DoubleVector -> b
        else            -> objectMapper.convertValue(checkNotNull(b))
    })
}
