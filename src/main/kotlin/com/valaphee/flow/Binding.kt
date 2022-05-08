package com.valaphee.flow

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.JsonIdentityReference
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.ObjectIdGenerators.PropertyGenerator
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
    private var value: (suspend () -> Any?)? = null
    private val channel by lazy { Channel<Any?>() }

    suspend fun set(value: Any?) = channel.send(value)

    fun set(value: suspend () -> Any?) {
        this.value = value
    }

    suspend operator fun invoke() = value?.invoke() ?: channel.receive()
}
