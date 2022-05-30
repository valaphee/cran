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

package com.valaphee.flow.path

import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.reflect.KClass

/**
 * @author Kevin Ludwig
 */
class DataPath(
    override val id: Int,
    private val objectMapper: ObjectMapper
) : Path() {
    internal var value: Any? = null
    internal var valueFunction: (suspend () -> Any?)? = null

    suspend fun get() = value ?: valueFunction?.invoke()

    suspend fun <T: Any> getOfTypeOrNull(type: KClass<T>): T? {
        val value = get()
        @Suppress("UNCHECKED_CAST") return if (type.isInstance(value)) value as T else objectMapper.convertValue(value, type.java)
    }

    fun set(value: Any?) {
        if (valueFunction != null) throw DataPathException.AlreadySet

        this.value = value
    }

    fun set(getValue: suspend () -> Any?) {
        if (value != null || this.valueFunction != null) throw DataPathException.AlreadySet

        this.valueFunction = getValue
    }

    suspend inline fun <reified T : Any> getOfTypeOrNull(): T? = getOfTypeOrNull(T::class)

    suspend inline fun <reified T : Any> getOfType(): T = checkNotNull(getOfTypeOrNull(T::class))
}
