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

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.DataPath
import com.valaphee.flow.LazyNode
import kotlin.math.max
import kotlin.reflect.KClass

/**
 * @author Kevin Ludwig
 */
abstract class MathNode : LazyNode() {
    @get:JsonProperty("in_a") abstract val inA: DataPath
    @get:JsonProperty("in_b") abstract val inB: DataPath
    @get:JsonProperty("out") abstract val out: DataPath

    companion object {
        private val typeOrder = listOf(
            Byte::class,
            Short::class,
            Int::class,
            Long::class,
            Float::class,
            Double::class
        )

        @JvmStatic
        protected fun outType(inA: KClass<*>, inB: KClass<*>) = typeOrder[max(typeOrder.indexOf(inA), typeOrder.indexOf(inB))]
    }
}
