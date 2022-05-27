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

package com.valaphee.flow.node.math.vector3

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.module.kotlin.convertValue
import com.valaphee.foundry.math.Double3
import com.valaphee.foundry.math.Float3
import com.valaphee.foundry.math.Int3

const val Vec3 = """{"type":"array","items":{"type":"number"},"minItems":3,"maxItems":3}"""

/**
 * @author Kevin Ludwig
 */
object Int3Serializer : JsonSerializer<Int3>() {
    override fun serialize(value: Int3, generator: JsonGenerator, serializer: SerializerProvider) {
        generator.writeArray(intArrayOf(value.x, value.y, value.z), 0, 3)
    }
}

/**
 * @author Kevin Ludwig
 */
object Int3Deserializer : JsonDeserializer<Int3>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): Int3 {
        val array = parser.readValueAs(IntArray::class.java)
        return Int3(array[0], array[1], array[2])
    }
}

/**
 * @author Kevin Ludwig
 */
object Double3Serializer : JsonSerializer<Double3>() {
    override fun serialize(value: Double3, generator: JsonGenerator, serializer: SerializerProvider) {
        generator.writeArray(doubleArrayOf(value.x, value.y, value.z), 0, 3)
    }
}

/**
 * @author Kevin Ludwig
 */
object Double3Deserializer : JsonDeserializer<Double3>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): Double3 {
        val array = parser.readValueAs(DoubleArray::class.java)
        return Double3(array[0], array[1], array[2])
    }
}

inline fun vector3Op(n: Any?, intOp: (Int3) -> Any, floatOp: (Float3) -> Any, doubleOp: (Double3) -> Any, objectMapper: ObjectMapper) = when (n) {
    is Int3    -> intOp   (n                                         )
    is Float3  -> floatOp (n                                         )
    is Double3 -> doubleOp(n                                         )
    else       -> doubleOp(objectMapper.convertValue(checkNotNull(n)))
}

inline fun vector3Op(a: Any?, b: Any?, intOp: (Int3, Int3) -> Any, floatOp: (Float3, Float3) -> Any, doubleOp: (Double3, Double3) -> Any, objectMapper: ObjectMapper): Any = when (a) {
    is Int3 -> when (b) {
        is Int3    -> intOp   (a            , b                                         )
        is Float3  -> floatOp (a.toFloat3() , b                                         )
        is Double3 -> doubleOp(a.toDouble3(), b                                         )
        else       -> doubleOp(a.toDouble3(), objectMapper.convertValue(checkNotNull(b)))
    }
    is Float3 -> when (b) {
        is Int3    -> floatOp (a            , b.toFloat3()                              )
        is Float3  -> floatOp (a            , b                                         )
        is Double3 -> doubleOp(a.toDouble3(), b                                         )
        else       -> doubleOp(a.toDouble3(), objectMapper.convertValue(checkNotNull(b)))
    }
    is Double3 -> when (b) {
        is Int3    -> doubleOp(a, b.toDouble3()                             )
        is Float3  -> doubleOp(a, b.toDouble3()                             )
        is Double3 -> doubleOp(a, b                                         )
        else       -> doubleOp(a, objectMapper.convertValue(checkNotNull(b)))
    }
    else -> doubleOp(objectMapper.convertValue(checkNotNull(a)), when (b) {
        is Int3    -> b.toDouble3()
        is Float3  -> b.toDouble3()
        is Double3 -> b
        else       -> objectMapper.convertValue(checkNotNull(b))
    })
}
