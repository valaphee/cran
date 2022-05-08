package com.valaphee.flow.control

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.Binding
import com.valaphee.flow.Node

/**
 * @author Kevin Ludwig
 */
class SelectNode(
    @get:JsonProperty("in") val `in`: Binding,
    @get:JsonProperty("select") val select: Map<Any?, Binding>,
    @get:JsonProperty("out") val out: Binding
) : Node() {
    override suspend fun evaluate() {
        out.set { select[`in`()]?.invoke() }
    }
}
