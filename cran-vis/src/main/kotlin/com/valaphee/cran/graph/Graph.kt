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
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.inject.Inject
import com.valaphee.cran.meta.Meta
import com.valaphee.cran.settings.Settings
import com.valaphee.cran.spec.Spec
import com.valaphee.cran.util.update
import eu.mihosoft.vrl.workflow.Connector
import eu.mihosoft.vrl.workflow.FlowFactory
import eu.mihosoft.vrl.workflow.VFlow
import eu.mihosoft.vrl.workflow.VNode
import javafx.beans.property.SimpleObjectProperty
import java.util.UUID
import kotlin.math.max
import kotlin.math.round

/**
 * @author Kevin Ludwig
 */
class Graph(
    @get:JsonProperty("id"  ) val id   : UUID                           = UUID.randomUUID(),
    @get:JsonProperty("name") var name : String                         = "New Graph",
                                  meta : Meta                           = Meta(emptyList()),
                                  nodes: List<MutableMap<String, Any?>> = emptyList()
) {
    @get:JsonProperty("meta") val meta = meta
        get() = field.copy(nodes = flow.nodes.map { Meta.Node(it.x, it.y) })
    @get:JsonProperty("nodes") val nodes: List<Map<String, Any?>>
        get() {
            val connections = mutableListOf<MutableList<Connector>>().apply { flow.allConnections.forEach { it.value.connections.forEach { connection -> find { it.contains(connection.sender) }?.add(connection.receiver) ?: find { it.contains(connection.receiver) }?.add(connection.sender) ?: run { this += mutableListOf(connection.sender, connection.receiver) } } } }.withIndex()
            var index = connections.lastOrNull()?.index?.let { it + 1 } ?: 0
            val embed = mutableListOf<Map<String, Any?>>()
            return flow.nodes.map { node ->
                val nodeValueObject = node.valueObject as NodeValueObject
                mapOf<String, Any?>("type" to nodeValueObject.spec.name) +  nodeValueObject.const.associate { it.spec.json to it.valueProperty.value } + node.inputs.associate { input ->
                    val connectorValueObject = input.valueObject as ConnectorValueObject
                    connectorValueObject.spec.json to (connections.find { it.value.contains(input) }?.index ?: connectorValueObject.value?.let {
                        embed += mapOf("type" to "Value", "value" to it, "out" to index, "embed" to true)
                        index++
                    } ?: index++)
                } + node.outputs.associate { output -> output.localId to (connections.find { it.value.contains(output) }?.index ?: index++) }
            } + embed
        }

    @Inject private lateinit var settings: Settings
    @get:JsonIgnore lateinit var spec: Spec
    @get:JsonIgnore val flow: VFlow by lazy {
        FlowFactory.newFlow().apply {
            isVisible = true

            val (embed, other) = nodes.partition { it["type"] == "Value" && it.getOrDefault("embed", false) as Boolean }
            val _embed = embed.associate { it["out"] as Int to it["value"] }
            mutableMapOf<Int, MutableList<Connector>>().apply {
                other.forEachIndexed { i, node ->
                    val type = node.remove("type") as String
                    val spec = spec.nodes.single { it.name == type }
                    newNode(spec, meta.nodes.getOrNull(i), settings).apply {
                        (valueObject as NodeValueObject).const.forEach { it.valueProperty.value = node[it.spec.json] }
                        connectors.forEach {
                            val connectorValueObject = it.valueObject as ConnectorValueObject
                            val connectionId = node[connectorValueObject.spec.json] as Int
                            if (connectorValueObject.spec.type == Spec.Node.Port.Type.InData) connectorValueObject.value = _embed[connectionId]

                            getOrPut(connectionId) { mutableListOf() } += it
                        }
                    }
                }
            }.values.forEach { it.forEach { connectorA -> if (connectorA.isOutput) it.forEach { connectorB -> if (connectorB.isInput) connect(connectorA, connectorB) } } }
        }
    }

    fun newNode(spec: Spec.Node, meta: Meta.Node?) = flow.newNode(spec, meta, settings)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Graph

        if (name != other.name) return false

        return true
    }

    override fun hashCode() = name.hashCode()

    companion object {
        private fun VFlow.newNode(spec: Spec.Node, meta: Meta.Node?, settings: Settings): VNode = newNode().apply {
            title = spec.name
            meta?.let {
                x = it.x
                y = it.y
            }

            xProperty().update { if (settings.gridX > 0) x = round(max(it, 0.0) / settings.gridX) * settings.gridX }
            yProperty().update { if (settings.gridY > 0) y = round(max(it, 0.0) / settings.gridY) * settings.gridY }
            widthProperty().update { if (settings.gridX > 0) width = round(it / settings.gridX) * settings.gridX }
            heightProperty().update { if (settings.gridY > 0) height = round(it / settings.gridY) * settings.gridY }

            val const = mutableListOf<NodeValueObject.Const>()
            spec.ports.forEach { portSpec ->
                when (portSpec.type) {
                    Spec.Node.Port.Type.InControl -> addInput("control").apply {
                        /*visualizationRequest[VisualizationRequest.KEY_CONNECTOR_PREFER_TOP_DOWN] = true*/
                        localId = portSpec.json
                        valueObject = ConnectorValueObject(portSpec)
                    }
                    Spec.Node.Port.Type.OutControl -> addOutput("control").apply {
                        /*visualizationRequest[VisualizationRequest.KEY_CONNECTOR_PREFER_TOP_DOWN] = true*/
                        localId = portSpec.json
                        valueObject = ConnectorValueObject(portSpec)
                    }
                    Spec.Node.Port.Type.InData -> addInput("data").apply {
                        localId = portSpec.json
                        valueObject = ConnectorValueObject(portSpec)
                    }
                    Spec.Node.Port.Type.OutData -> addOutput("data").apply {
                        localId = portSpec.json
                        valueObject = ConnectorValueObject(portSpec)
                    }
                    Spec.Node.Port.Type.Const -> const += NodeValueObject.Const(portSpec, SimpleObjectProperty(null))
                }
            }

            valueObject = NodeValueObject(spec, const)
        }
    }
}
