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
import com.google.inject.Inject
import com.valaphee.flow.meta.Meta
import com.valaphee.flow.spec.Spec
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
            val embed = mutableListOf<Map<String, Any?>>()
            return flow.nodes.map {
                mapOf<String, Any?>("type" to (it.valueObject.value as Spec.Node).json) + it.inputs.associate { input ->
                    val (_, _const) = input.valueObject.value as Pair<*, *>
                    input.localId to (connections.singleOrNull { it.value.value.contains(input) }?.index ?: _const?.let {
                        if (input.type != "const") {
                            embed += mapOf("type" to "com.valaphee.flow.Value", "value" to it, "out" to index, "embed" to true)
                            index++
                        } else it
                    } ?: index++)
                } + it.outputs.associate { output ->
                    output.localId to (connections.singleOrNull { it.value.key == output }?.index ?: index++)
                }
            } + embed
        }

    @Inject private lateinit var spec: Spec
    @get:JsonIgnore val flow: VFlow by lazy {
        FlowFactory.newFlow().apply {
            isVisible = true

            val (embed, other) = graph.partition { it["type"] == "com.valaphee.flow.Value" && it.getOrDefault("embed", false) as Boolean }
            val _embed = embed.associate { it["out"] as Int to it["value"] }
            val connectors = other.mapIndexed { i, node ->
                val type = node.remove("type") as String
                val nodeSpec = spec.nodes.single { it.json == type }
                val _node = newNode().apply {
                    title = nodeSpec.name
                    meta.nodes.getOrNull(i)?.let {
                        x = it.x
                        y = it.y
                    }
                    valueObject.value = nodeSpec
                    /*selectableProperty().value = false*/
                }
                nodeSpec.ports.mapNotNull { nodePortSpec ->
                    when (nodePortSpec.type) {
                        Spec.Node.Port.Type.InControl -> node[nodePortSpec.json] as Int to _node.addInput("control").apply {
                            /*visualizationRequest[VisualizationRequest.KEY_CONNECTOR_PREFER_TOP_DOWN] = true*/
                            localId = nodePortSpec.json
                            valueObject.value = nodePortSpec to null
                        }
                        Spec.Node.Port.Type.OutControl -> node[nodePortSpec.json] as Int to _node.addOutput("control").apply {
                            /*visualizationRequest[VisualizationRequest.KEY_CONNECTOR_PREFER_TOP_DOWN] = true*/
                            localId = nodePortSpec.json
                            valueObject.value = nodePortSpec to null
                        }
                        Spec.Node.Port.Type.InData -> {
                            val connectionId = node[nodePortSpec.json] as Int
                            connectionId to _node.addInput("data").apply {
                                localId = nodePortSpec.json
                                valueObject.value = nodePortSpec to _embed[connectionId]
                            }
                        }
                        Spec.Node.Port.Type.OutData -> node[nodePortSpec.json] as Int to _node.addOutput("data").apply {
                            localId = nodePortSpec.json
                            valueObject.value = nodePortSpec to null
                        }
                        Spec.Node.Port.Type.Const -> {
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
    }

    fun newNode(spec: Spec.Node, meta: Meta.Node) {
        val node = flow.newNode().apply {
            title = spec.name
            x = meta.x
            y = meta.y
            valueObject.value = spec
            /*selectableProperty().value = false*/
        }
        spec.ports.forEach { nodePortSpec ->
            when (nodePortSpec.type) {
                Spec.Node.Port.Type.InControl -> node.addInput("control").apply {
                    /*visualizationRequest[VisualizationRequest.KEY_CONNECTOR_PREFER_TOP_DOWN] = true*/
                    localId = nodePortSpec.json
                    valueObject.value = nodePortSpec to null
                }
                Spec.Node.Port.Type.OutControl -> node.addOutput("control").apply {
                    /*visualizationRequest[VisualizationRequest.KEY_CONNECTOR_PREFER_TOP_DOWN] = true*/
                    localId = nodePortSpec.json
                    valueObject.value = nodePortSpec to null
                }
                Spec.Node.Port.Type.InData -> node.addInput("data").apply {
                    localId = nodePortSpec.json
                    valueObject.value = nodePortSpec to null
                }
                Spec.Node.Port.Type.OutData -> node.addOutput("data").apply {
                    localId = nodePortSpec.json
                    valueObject.value = nodePortSpec to null
                }
                Spec.Node.Port.Type.Const -> node.addInput("const").apply {
                    localId = nodePortSpec.json
                    valueObject.value = nodePortSpec to null
                }
            }
        }
    }
}
