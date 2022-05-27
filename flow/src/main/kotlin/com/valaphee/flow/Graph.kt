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

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.node.NullNode
import com.valaphee.flow.nesting.ControlInput
import com.valaphee.flow.nesting.ControlOutput
import com.valaphee.flow.nesting.DataInput
import com.valaphee.flow.nesting.DataOutput
import com.valaphee.flow.spec.Spec
import java.util.UUID

/**
 * @author Kevin Ludwig
 */
abstract class Graph {
    @get:JsonProperty("id"   ) abstract val id   : UUID
    @get:JsonProperty("name" ) abstract val name : String
    @get:JsonProperty("nodes") abstract val nodes: List<Node>

    open fun initialize(instance: Scope) {
        nodes.forEach { it.initialize(instance) }
    }

    open fun shutdown() {
        nodes.forEach { it.shutdown() }
    }

    fun toSpec() = Spec.Node(name, "", nodes.mapNotNull {
        when (it) {
            is ControlInput -> Spec.Node.Port(it.name, it.json, Spec.Node.Port.Type.InControl, NullNode.instance)
            is ControlOutput -> Spec.Node.Port(it.name, it.json, Spec.Node.Port.Type.OutControl, NullNode.instance)
            is DataInput -> Spec.Node.Port(it.name, it.json, Spec.Node.Port.Type.InData, NullNode.instance)
            is DataOutput -> Spec.Node.Port(it.name, it.json, Spec.Node.Port.Type.OutData, NullNode.instance)
            else -> null
        }
    })

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Graph

        if (name != other.name) return false

        return true
    }

    override fun hashCode() = name.hashCode()
}
