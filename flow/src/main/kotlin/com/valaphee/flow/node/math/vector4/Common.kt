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

package com.valaphee.flow.node.math.vector4

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.module.kotlin.convertValue
import com.valaphee.foundry.math.Double4
import com.valaphee.foundry.math.Float4
import com.valaphee.foundry.math.Int4

const val Vec4 = """{"type":"array","items":{"type":"number"},"minItems":4,"maxItems":4}"""

/**
 * @author Kevin Ludwig
 */
object Int4Serializer : JsonSerializer<Int4>() {
    override fun serialize(value: Int4, generator: JsonGenerator, serializer: SerializerProvider) {
        generator.writeArray(intArrayOf(value.x, value.y, value.z, value.w), 0, 4)
    }
}

/**
 * @author Kevin Ludwig
 */
object Int4Deserializer : JsonDeserializer<Int4>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): Int4 {
        val array = parser.readValueAs(IntArray::class.java)
        return Int4(array[0], array[1], array[2], array[3])
    }
}

/**
 * @author Kevin Ludwig
 */
object Double4Serializer : JsonSerializer<Double4>() {
    override fun serialize(value: Double4, generator: JsonGenerator, serializer: SerializerProvider) {
        generator.writeArray(doubleArrayOf(value.x, value.y, value.z, value.w), 0, 4)
    }
}

/**
 * @author Kevin Ludwig
 */
object Double4Deserializer : JsonDeserializer<Double4>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): Double4 {
        val array = parser.readValueAs(DoubleArray::class.java)
        return Double4(array[0], array[1], array[2], array[3])
    }
}

inline fun vector4Op(n: Any?, intOp: (Int4) -> Any, floatOp: (Float4) -> Any, doubleOp: (Double4) -> Any, objectMapper: ObjectMapper) = when (n) {
    is Int4    -> intOp   (n                                         )
    is Float4  -> floatOp (n                                         )
    is Double4 -> doubleOp(n                                         )
    else       -> doubleOp(objectMapper.convertValue(checkNotNull(n)))
}

inline fun vector4Op(a: Any?, b: Any?, intOp: (Int4, Int4) -> Any, floatOp: (Float4, Float4) -> Any, doubleOp: (Double4, Double4) -> Any, objectMapper: ObjectMapper): Any = when (a) {
    is Int4 -> when (b) {
        is Int4    -> intOp   (a            , b                                         )
        is Float4  -> floatOp (a.toFloat4() , b                                         )
        is Double4 -> doubleOp(a.toDouble4(), b                                         )
        else       -> doubleOp(a.toDouble4(), objectMapper.convertValue(checkNotNull(b)))
    }
    is Float4 -> when (b) {
        is Int4    -> floatOp (a            , b.toFloat4()                              )
        is Float4  -> floatOp (a            , b                                         )
        is Double4 -> doubleOp(a.toDouble4(), b                                         )
        else       -> doubleOp(a.toDouble4(), objectMapper.convertValue(checkNotNull(b)))
    }
    is Double4 -> when (b) {
        is Int4    -> doubleOp(a, b.toDouble4()                             )
        is Float4  -> doubleOp(a, b.toDouble4()                             )
        is Double4 -> doubleOp(a, b                                         )
        else       -> doubleOp(a, objectMapper.convertValue(checkNotNull(b)))
    }
    else -> doubleOp(objectMapper.convertValue(checkNotNull(a)), when (b) {
        is Int4    -> b.toDouble4()
        is Float4  -> b.toDouble4()
        is Double4 -> b
        else       -> objectMapper.convertValue(checkNotNull(b))
    })
}
