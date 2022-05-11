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

package com.valaphee.flow.graph

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.Spec
import com.valaphee.flow.meta.Meta
import eu.mihosoft.vrl.workflow.Connector
import eu.mihosoft.vrl.workflow.FlowFactory
import eu.mihosoft.vrl.workflow.VFlow
import java.util.UUID

/**
 * @author Kevin Ludwig
 */
class Graph(
    @get:JsonProperty("id"  ) val id   : UUID                           = UUID.randomUUID(),
                                  meta : Meta                           = Meta(id.toString(), emptyList()),
                                  graph: List<MutableMap<String, Any?>> = emptyList()
) {
    @get:JsonProperty("meta") val meta = meta
        get() = field.copy(nodes = flow.nodes.map { Meta.Node(it.x, it.y) })
    @get:JsonProperty("graph") val graph: List<Map<String, Any?>>
        get() {
            val connections = mutableMapOf<Connector, MutableList<Connector>>().apply { flow.allConnections.forEach { it.value.connections.forEach { getOrPut(it.sender) { mutableListOf() } += it.receiver } } }.entries.withIndex()
            var index = connections.lastOrNull()?.index?.let { it + 1 } ?: 0
            return flow.nodes.map {
                mapOf<String, Any?>("type" to (it.valueObject.value as com.valaphee.flow.spec.Spec.Node).json) + it.inputs.associate { input ->
                    val (spec, const) = input.valueObject.value as Pair<com.valaphee.flow.spec.Spec.Node.Port, Any?>
                    input.localId to (const ?: connections.singleOrNull { it.value.value.contains(input) }?.index ?: if (spec.optional) null else index++)
                } + it.outputs.associate { output ->
                    val (spec, _) = output.valueObject.value as Pair<com.valaphee.flow.spec.Spec.Node.Port, Any?>
                    output.localId to (connections.singleOrNull { it.value.key == output }?.index ?: if (spec.optional) null else index++)
                }
            }
        }

    @get:JsonIgnore val flow: VFlow = FlowFactory.newFlow().apply {
        isVisible = true

        val connectors = graph.mapIndexed { i, node ->
            val type = node.remove("type") as String
            val nodeSpec = Spec.nodes.single { it.json == type }
            val _node = newNode().apply {
                title = nodeSpec.name
                meta.nodes.getOrNull(i)?.let {
                    x = it.x
                    y = it.y
                }
                valueObject.value = nodeSpec
                selectableProperty().value = false
            }
            nodeSpec.ports.mapNotNull { nodePortSpec ->
                when (nodePortSpec.type) {
                    com.valaphee.flow.spec.Spec.Node.Port.Type.InControl -> node[nodePortSpec.json] as Int to _node.addInput("control").apply {
                        /*visualizationRequest[VisualizationRequest.KEY_CONNECTOR_PREFER_TOP_DOWN] = true*/
                        localId = nodePortSpec.json
                        valueObject.value = nodePortSpec to null
                    }
                    com.valaphee.flow.spec.Spec.Node.Port.Type.OutControl -> node[nodePortSpec.json] as Int to _node.addOutput("control").apply {
                        /*visualizationRequest[VisualizationRequest.KEY_CONNECTOR_PREFER_TOP_DOWN] = true*/
                        localId = nodePortSpec.json
                        valueObject.value = nodePortSpec to null
                    }
                    com.valaphee.flow.spec.Spec.Node.Port.Type.InData -> node[nodePortSpec.json] as Int to _node.addInput("data").apply {
                        localId = nodePortSpec.json
                        valueObject.value = nodePortSpec to null
                    }
                    com.valaphee.flow.spec.Spec.Node.Port.Type.OutData -> node[nodePortSpec.json] as Int to _node.addOutput("data").apply {
                        localId = nodePortSpec.json
                        valueObject.value = nodePortSpec to null
                    }
                    com.valaphee.flow.spec.Spec.Node.Port.Type.Const -> {
                        _node.addInput("const").apply {
                            localId = nodePortSpec.json
                            valueObject.value = nodePortSpec to node[nodePortSpec.json]
                        }
                        null
                    }
                }
            }.toMap()
        }
        graph.zip(connectors).forEach { it.first.entries.forEach { port -> it.second[port.value]?.let { connectorA -> connectors.forEach { it[port.value]?.let { connectorB -> connect(connectorA, connectorB) } } } } }
    }

    fun newNode(spec: com.valaphee.flow.spec.Spec.Node, meta: Meta.Node) {
        val node = flow.newNode().apply {
            title = spec.name
            x = meta.x
            y = meta.y
            valueObject.value = spec
            selectableProperty().value = false
        }
        spec.ports.forEach { nodePortSpec ->
            when (nodePortSpec.type) {
                com.valaphee.flow.spec.Spec.Node.Port.Type.InControl -> node.addInput("control").apply {
                    /*visualizationRequest[VisualizationRequest.KEY_CONNECTOR_PREFER_TOP_DOWN] = true*/
                    localId = nodePortSpec.json
                    valueObject.value = nodePortSpec to null
                }
                com.valaphee.flow.spec.Spec.Node.Port.Type.OutControl -> node.addOutput("control").apply {
                    /*visualizationRequest[VisualizationRequest.KEY_CONNECTOR_PREFER_TOP_DOWN] = true*/
                    localId = nodePortSpec.json
                    valueObject.value = nodePortSpec to null
                }
                com.valaphee.flow.spec.Spec.Node.Port.Type.InData -> node.addInput("data").apply {
                    localId = nodePortSpec.json
                    valueObject.value = nodePortSpec to null
                }
                com.valaphee.flow.spec.Spec.Node.Port.Type.OutData -> node.addOutput("data").apply {
                    localId = nodePortSpec.json
                    valueObject.value = nodePortSpec to null
                }
                com.valaphee.flow.spec.Spec.Node.Port.Type.Const -> node.addInput("const").apply {
                    localId = nodePortSpec.json
                    valueObject.value = nodePortSpec to null
                }
            }
        }
    }
}
