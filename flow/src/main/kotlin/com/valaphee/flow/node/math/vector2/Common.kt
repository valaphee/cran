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

package com.valaphee.flow.node.math.vector2

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.module.kotlin.convertValue
import com.valaphee.foundry.math.Double2
import com.valaphee.foundry.math.Float2
import com.valaphee.foundry.math.Int2

const val Vec2 = """{"type":"array","items":{"type":"number"},"minItems":2,"maxItems":2}"""

/**
 * @author Kevin Ludwig
 */
object Int2Serializer : JsonSerializer<Int2>() {
    override fun serialize(value: Int2, generator: JsonGenerator, serializer: SerializerProvider) {
        generator.writeArray(intArrayOf(value.x, value.y), 0, 2)
    }
}

/**
 * @author Kevin Ludwig
 */
object Int2Deserializer : JsonDeserializer<Int2>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): Int2 {
        val array = parser.readValueAs(IntArray::class.java)
        return Int2(array[0], array[1])
    }
}

/**
 * @author Kevin Ludwig
 */
object Double2Serializer : JsonSerializer<Double2>() {
    override fun serialize(value: Double2, generator: JsonGenerator, serializer: SerializerProvider) {
        generator.writeArray(doubleArrayOf(value.x, value.y), 0, 2)
    }
}

/**
 * @author Kevin Ludwig
 */
object Double2Deserializer : JsonDeserializer<Double2>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): Double2 {
        val array = parser.readValueAs(DoubleArray::class.java)
        return Double2(array[0], array[1])
    }
}

inline fun vector2Op(n: Any?, intOp: (Int2) -> Any, floatOp: (Float2) -> Any, doubleOp: (Double2) -> Any, objectMapper: ObjectMapper) = when (n) {
    is Int2    -> intOp   (n                                         )
    is Float2  -> floatOp (n                                         )
    is Double2 -> doubleOp(n                                         )
    else       -> doubleOp(objectMapper.convertValue(checkNotNull(n)))
}

inline fun vector2Op(a: Any?, b: Any?, intOp: (Int2, Int2) -> Any, floatOp: (Float2, Float2) -> Any, doubleOp: (Double2, Double2) -> Any, objectMapper: ObjectMapper) = when (a) {
    is Int2 -> when (b) {
        is Int2    -> intOp   (a            , b                                         )
        is Float2  -> floatOp (a.toFloat2() , b                                         )
        is Double2 -> doubleOp(a.toDouble2(), b                                         )
        else       -> doubleOp(a.toDouble2(), objectMapper.convertValue(checkNotNull(b)))
    }
    is Float2 -> when (b) {
        is Int2    -> floatOp (a            , b.toFloat2()                              )
        is Float2  -> floatOp (a            , b                                         )
        is Double2 -> doubleOp(a.toDouble2(), b                                         )
        else       -> doubleOp(a.toDouble2(), objectMapper.convertValue(checkNotNull(b)))
    }
    is Double2 -> when (b) {
        is Int2    -> doubleOp(a, b.toDouble2()                             )
        is Float2  -> doubleOp(a, b.toDouble2()                             )
        is Double2 -> doubleOp(a, b                                         )
        else       -> doubleOp(a, objectMapper.convertValue(checkNotNull(b)))
    }
    else -> doubleOp(objectMapper.convertValue(checkNotNull(a)), when (b) {
        is Int2    -> b.toDouble2()
        is Float2  -> b.toDouble2()
        is Double2 -> b
        else       -> objectMapper.convertValue(checkNotNull(b))
    })
}
