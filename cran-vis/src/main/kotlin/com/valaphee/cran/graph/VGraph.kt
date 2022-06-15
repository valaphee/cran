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

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.google.inject.Inject
import com.valaphee.cran.meta.Meta
import com.valaphee.cran.node.Node
import com.valaphee.cran.node.Value
import com.valaphee.cran.path.Entry
import com.valaphee.cran.settings.Settings
import eu.mihosoft.vrl.workflow.Connections
import eu.mihosoft.vrl.workflow.Connector
import eu.mihosoft.vrl.workflow.VNode

/**
 * @author Kevin Ludwig
 */
abstract class VGraph : GraphWithMeta() {
    @get:JsonIgnore protected abstract val _nodes: List<VNode>
    @get:JsonIgnore protected abstract val _connections: Map<String, Connections>
    override val meta get() = Meta(nodes = _nodes.map { Meta.Node(it.x, it.y) })
    override val nodes: List<Node>
        get() {
            val connections = mutableListOf<MutableList<Connector>>().apply { _connections.forEach { it.value.connections.forEach { connection -> find { it.contains(connection.sender) }?.add(connection.receiver) ?: find { it.contains(connection.receiver) }?.add(connection.sender) ?: run { this += mutableListOf(connection.sender, connection.receiver) } } } }.withIndex()
            var index = connections.lastOrNull()?.index?.let { it + 1 } ?: 0
            val embed = mutableListOf<Value>()
            return _nodes.map { node ->
                val nodeValueObject = node.valueObject as NodeValueObject
                val (multiInputs, otherInputs) = node.inputs.partition { (it.valueObject as ConnectorValueObject).spec.multi }
                val (multiOutputs, otherOutputs) = node.outputs.partition { (it.valueObject as ConnectorValueObject).spec.multi }
                objectMapper.convertValue<Node>(mapOf<String, Any?>("type" to nodeValueObject.spec.name) + nodeValueObject.const.associate { it.spec.json to it.value } + otherInputs.associate { input ->
                    val connectorValueObject = input.valueObject as ConnectorValueObject
                    input.localId to (connections.find { it.value.contains(input) }?.index ?: connectorValueObject.value?.let {
                        embed += Value("Value", it, index, true)
                        index++
                    } ?: -1)
                } + multiInputs.groupBy { it.localId }.mapValues {
                    it.value.map { input ->
                        val connectorValueObject = input.valueObject as ConnectorValueObject
                        Entry(connectorValueObject.multiKeyProperty!!.value, (connections.find { it.value.contains(input) }?.index ?: connectorValueObject.value?.let {
                            embed += Value("Value", it, index, true)
                            index++
                        } ?: -1))
                    }
                } + otherOutputs.associate { output -> output.localId to (connections.find { it.value.contains(output) }?.index ?: -1) } + multiOutputs.groupBy { it.localId }.mapValues { it.value.map { output -> Entry((output.valueObject as ConnectorValueObject).multiKeyProperty!!.value, (connections.find { it.value.contains(output) }?.index ?: -1)) } })
            } + embed
        }

    @get:JsonIgnore @Inject protected lateinit var objectMapper: ObjectMapper
    @get:JsonIgnore @Inject protected lateinit var settings: Settings
}
