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

package com.valaphee.flow

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonIdentityReference
import com.fasterxml.jackson.annotation.ObjectIdGenerator
import com.fasterxml.jackson.annotation.ObjectIdGenerators
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver

/**
 * @author Kevin Ludwig
 */
@JsonIdentityInfo(property = "id", generator = ObjectIdGenerators.PropertyGenerator::class, resolver = DataPath.IdResolver::class)
@JsonIdentityReference(alwaysAsId = true)
class DataPath(
    override val id: Int
) : Path() {
    private var value: Any? = null
    private var valueFunction: (suspend () -> Any?)? = null

    suspend fun get() = value ?: valueFunction?.invoke()

    fun set(value: Any?) {
        if (this.value != null || valueFunction != null) throw DataPathException.Undefined

        this.value = value
    }

    fun set(getValue: suspend () -> Any?) {
        if (value != null || this.valueFunction != null) throw DataPathException.AlreadySet

        this.valueFunction = getValue
    }

    class IdResolver : SimpleObjectIdResolver() {
        override fun resolveId(id: ObjectIdGenerator.IdKey) = super.resolveId(id) ?: DataPath(id.key as Int).also { bindItem(id, it) }

        override fun newForDeserialization(context: Any?) = IdResolver()
    }
}

suspend inline fun <reified T> DataPath.getOrThrow(): T {
    val value = get()
    return if (value is T) value else throw DataPathException.InvalidType
}
