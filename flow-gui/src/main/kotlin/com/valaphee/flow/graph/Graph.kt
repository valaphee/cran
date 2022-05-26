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
import com.valaphee.flow.settings.Settings
import com.valaphee.flow.spec.Spec
import eu.mihosoft.vrl.workflow.Connector
import eu.mihosoft.vrl.workflow.FlowFactory
import eu.mihosoft.vrl.workflow.VFlow
import eu.mihosoft.vrl.workflow.VNode
import tornadofx.onChange
import java.util.UUID
import kotlin.math.round

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
                mapOf<String, Any?>("type" to (it.valueObject.value as Spec.Node).name) + it.inputs.associate { input ->
                    val value = input.valueObject.value as ConnectorValue
                    input.localId to (connections.singleOrNull { it.value.value.contains(input) }?.index ?: value.value?.let {
                        if (input.type != "const") {
                            embed += mapOf("type" to "com.valaphee.flow.Value", "value" to it, "out" to index, "embed" to true)
                            index++
                        } else it
                    } ?: index++)
                } + it.outputs.associate { output -> output.localId to (connections.singleOrNull { it.value.key == output }?.index ?: index++) }
            } + embed
        }

    @Inject private lateinit var settings: Settings
    @get:JsonIgnore lateinit var spec: Spec
    @get:JsonIgnore val flow: VFlow by lazy {
        FlowFactory.newFlow().apply {
            isVisible = true

            val (embed, other) = graph.partition { it["type"] == "com.valaphee.flow.Value" && it.getOrDefault("embed", false) as Boolean }
            val _embed = embed.associate { it["out"] as Int to it["value"] }
            val connectors = other.mapIndexed { i, node ->
                val type = node.remove("type") as String
                val nodeSpec = spec.nodes.single { it.name == type }
                newNode(nodeSpec, meta.nodes.getOrNull(i), settings).connectors.mapNotNull {
                    val value = (it.valueObject.value as ConnectorValue)
                    if (value.spec.type == Spec.Node.Port.Type.Const) {
                        value.value = node[value.spec.json]

                        null
                    } else {
                        val connectionId = node[value.spec.json] as Int
                        if (value.spec.type == Spec.Node.Port.Type.InData) value.value = _embed[connectionId]

                        connectionId to it
                    }
                }.toMap()
            }
            graph.zip(connectors).forEach { it.first.entries.forEach { port -> it.second[port.value]?.let { connectorA -> connectors.forEach { it[port.value]?.let { connectorB -> connect(connectorA, connectorB) } } } } }
        }
    }

    fun newNode(spec: Spec.Node, meta: Meta.Node?) = flow.newNode(spec, meta, settings)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Graph

        if (id != other.id) return false

        return true
    }

    override fun hashCode() = id.hashCode()

    companion object {
        private fun VFlow.newNode(spec: Spec.Node, meta: Meta.Node?, settings: Settings): VNode = newNode().apply {
            title = spec.name
            xProperty().onChange { if (settings.gridX > 0) x = round(it / settings.gridX) * settings.gridX }
            yProperty().onChange { if (settings.gridY > 0) y = round(it / settings.gridY) * settings.gridY }
            widthProperty().onChange { if (settings.gridX > 0) width = round(it / settings.gridX) * settings.gridX }
            heightProperty().onChange { if (settings.gridY > 0) height = round(it / settings.gridY) * settings.gridY }
            meta?.let {
                x = it.x
                y = it.y
            }
            valueObject.value = spec

            spec.ports.forEach { nodePortSpec ->
                when (nodePortSpec.type) {
                    Spec.Node.Port.Type.InControl -> addInput("control").apply {
                        /*visualizationRequest[VisualizationRequest.KEY_CONNECTOR_PREFER_TOP_DOWN] = true*/
                        localId = nodePortSpec.json
                        valueObject.value = ConnectorValue(nodePortSpec)
                    }
                    Spec.Node.Port.Type.OutControl -> addOutput("control").apply {
                        /*visualizationRequest[VisualizationRequest.KEY_CONNECTOR_PREFER_TOP_DOWN] = true*/
                        localId = nodePortSpec.json
                        valueObject.value = ConnectorValue(nodePortSpec)
                    }
                    Spec.Node.Port.Type.InData -> addInput("data").apply {
                        localId = nodePortSpec.json
                        valueObject.value = ConnectorValue(nodePortSpec)
                    }
                    Spec.Node.Port.Type.OutData -> addOutput("data").apply {
                        localId = nodePortSpec.json
                        valueObject.value = ConnectorValue(nodePortSpec)
                    }
                    Spec.Node.Port.Type.Const -> addInput("const").apply {
                        localId = nodePortSpec.json
                        valueObject.value = ConnectorValue(nodePortSpec)
                    }
                }
            }
        }
    }
}
