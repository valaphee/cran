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

package com.valaphee.cran.graph

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.valaphee.cran.node.Node
import com.valaphee.cran.node.nesting.ControlInput
import com.valaphee.cran.node.nesting.ControlOutput
import com.valaphee.cran.node.nesting.DataInput
import com.valaphee.cran.node.nesting.DataOutput
import com.valaphee.cran.spec.Spec

/**
 * @author Kevin Ludwig
 */
abstract class Graph {
    @get:JsonProperty("name" ) abstract val name : String
    @get:JsonProperty("nodes") abstract val nodes: List<Node>

    fun toSpec() = Spec.Node(name, null, nodes.mapNotNull {
        when (it) {
            is ControlInput  -> Spec.Node.Port(it.name, it.json, Spec.Node.Port.Type.InControl , NullNode.instance                   )
            is ControlOutput -> Spec.Node.Port(it.name, it.json, Spec.Node.Port.Type.OutControl, NullNode.instance                   )
            is DataInput     -> Spec.Node.Port(it.name, it.json, Spec.Node.Port.Type.InData    , ObjectNode(JsonNodeFactory.instance))
            is DataOutput    -> Spec.Node.Port(it.name, it.json, Spec.Node.Port.Type.OutData   , ObjectNode(JsonNodeFactory.instance))
            else             -> null
        }
    })

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Graph

        if (name.lowercase() != other.name.lowercase()) return false

        return true
    }

    override fun hashCode() = name.lowercase().hashCode()
}
