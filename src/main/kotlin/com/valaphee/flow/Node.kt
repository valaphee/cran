package com.valaphee.flow

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 * @author Kevin Ludwig
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type")
abstract class Node {
    @get:JsonProperty("type") val type: String get() = this::class.java.name

    abstract suspend fun evaluate()
}
