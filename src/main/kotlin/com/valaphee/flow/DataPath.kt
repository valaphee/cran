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
import kotlinx.coroutines.CompletableDeferred
import java.util.UUID

/**
 * @author Kevin Ludwig
 */
@JsonIdentityInfo(property = "id", generator = ObjectIdGenerators.PropertyGenerator::class, resolver = DataPath.IdResolver::class)
@JsonIdentityReference(alwaysAsId = true)
class DataPath(
    override val id: UUID
) : Path() {
    private val deferred = CompletableDeferred<suspend () -> Any?>()

    suspend fun get() = deferred.await()()

    fun set(value: suspend () -> Any?) {
        deferred.complete(value)
    }

    class IdResolver : SimpleObjectIdResolver() {
        override fun resolveId(id: ObjectIdGenerator.IdKey) = super.resolveId(id) ?: DataPath(id.key as UUID).also { bindItem(id, it) }

        override fun newForDeserialization(context: Any?) = IdResolver()
    }
}