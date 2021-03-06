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

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import jdk.incubator.vector.DoubleVector
import jdk.incubator.vector.IntVector

const val Vec  = """{"type":"array","items":{"type":"number"}}"""
const val Vec2 = """{"type":"array","items":{"type":"number"},"minItems":2,"maxItems":2}"""
const val Vec3 = """{"type":"array","items":{"type":"number"},"minItems":3,"maxItems":3}"""
const val Vec4 = """{"type":"array","items":{"type":"number"},"minItems":4,"maxItems":4}"""

fun intVectorSpeciesByLength(length: Int) = when (length) {
    1, 2                          -> IntVector.SPECIES_64
    3, 4                          -> IntVector.SPECIES_128
    5, 6, 7, 8                    -> IntVector.SPECIES_256
    9, 10, 11, 12, 13, 14, 15, 16 -> IntVector.SPECIES_512
    else                          -> null
}

/**
 * @author Kevin Ludwig
 */
object IntVectorSerializer : JsonSerializer<IntVector>() {
    override fun serialize(value: IntVector, generator: JsonGenerator, serializer: SerializerProvider) {
        val array = value.toArray()
        generator.writeArray(array, 0, array.size)
    }
}

/**
 * @author Kevin Ludwig
 */
object IntVectorDeserializer : JsonDeserializer<IntVector>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): IntVector {
        val array = parser.readValueAs(IntArray::class.java)
        return when (array.size) {
            1                         -> IntVector.fromArray(IntVector.SPECIES_64 , array.copyOf(2), 0)
            2                         -> IntVector.fromArray(IntVector.SPECIES_64 , array          , 0)
            3                         -> IntVector.fromArray(IntVector.SPECIES_128, array.copyOf(4), 0)
            4                         -> IntVector.fromArray(IntVector.SPECIES_128, array          , 0)
            5, 6, 7                   -> IntVector.fromArray(IntVector.SPECIES_256, array.copyOf(8), 0)
            8                         -> IntVector.fromArray(IntVector.SPECIES_256, array          , 0)
            9, 10, 11, 12, 13, 14, 15 -> IntVector.fromArray(IntVector.SPECIES_512, array          , 0)
            16                        -> IntVector.fromArray(IntVector.SPECIES_512, array          , 0)
            else                      -> error("${array.size}")
        }
    }
}

fun doubleVectorSpeciesByLength(length: Int) = when (length) {
    1          -> DoubleVector.SPECIES_64
    2          -> DoubleVector.SPECIES_128
    3, 4       -> DoubleVector.SPECIES_256
    5, 6, 7, 8 -> DoubleVector.SPECIES_512
    else       -> null
}

/**
 * @author Kevin Ludwig
 */
object DoubleVectorSerializer : JsonSerializer<DoubleVector>() {
    override fun serialize(value: DoubleVector, generator: JsonGenerator, serializer: SerializerProvider) {
        val array = value.toArray()
        generator.writeArray(array, 0, array.size)
    }
}

/**
 * @author Kevin Ludwig
 */
object DoubleVectorDeserializer : JsonDeserializer<DoubleVector>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): DoubleVector {
        val array = parser.readValueAs(DoubleArray::class.java)
        return when (array.size) {
            1       -> DoubleVector.fromArray(DoubleVector.SPECIES_64 , array          , 0)
            2       -> DoubleVector.fromArray(DoubleVector.SPECIES_128, array          , 0)
            3       -> DoubleVector.fromArray(DoubleVector.SPECIES_256, array.copyOf(4), 0)
            4       -> DoubleVector.fromArray(DoubleVector.SPECIES_256, array          , 0)
            5, 6, 7 -> DoubleVector.fromArray(DoubleVector.SPECIES_512, array.copyOf(8), 0)
            8       -> DoubleVector.fromArray(DoubleVector.SPECIES_512, array          , 0)
            else    -> error("${array.size}")
        }
    }
}
