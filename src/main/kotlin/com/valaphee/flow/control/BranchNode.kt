package com.valaphee.flow.control

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.Binding
import com.valaphee.flow.Node

/**
 * @author Kevin Ludwig
 */
class BranchNode(
    @get:JsonProperty("in") val `in`: Binding,
    @get:JsonProperty("when") val `when`: Map<Any?, Binding>
) : Node() {
    override suspend fun evaluate() {
        while (true) `when`[`in`()]?.set(Unit)
    }
}
