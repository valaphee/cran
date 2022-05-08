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
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.ObjectIdGenerators.PropertyGenerator
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import java.util.UUID

/**
 * @author Kevin Ludwig
 */
@JsonIdentityInfo(property = "id", generator = PropertyGenerator::class, resolver = BindingIdResolver::class)
@JsonIdentityReference(alwaysAsId = true)
class Binding(
    @get:JsonProperty val id: UUID
) {
    private val deferred = CompletableDeferred<suspend () -> Any?>()
    private var channel: Channel<Any?>? = null

    suspend fun get() = deferred.await()()

    suspend fun set() = set(null)

    suspend fun set(value: Any?) {
        /*check(!deferred.isCompleted || channel != null)*/
        (channel ?: Channel<Any?>().also { channel = it }).let {
            deferred.complete { it.receive() }
            it.send(value)
        }
    }

    fun set(value: suspend () -> Any?) {
        /*check(!deferred.isCompleted)*/
        deferred.complete(value)
    }
}
