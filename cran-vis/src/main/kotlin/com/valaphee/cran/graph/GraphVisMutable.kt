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
import com.fasterxml.jackson.module.kotlin.convertValue
import com.valaphee.cran.meta.Meta
import com.valaphee.cran.node.Node
import com.valaphee.cran.node.Value
import com.valaphee.cran.path.Entry
import com.valaphee.cran.settings.Settings
import com.valaphee.cran.spec.Spec
import com.valaphee.cran.spec.SpecLookup
import com.valaphee.cran.util.update
import eu.mihosoft.vrl.workflow.Connections
import eu.mihosoft.vrl.workflow.Connector
import eu.mihosoft.vrl.workflow.FlowFactory
import eu.mihosoft.vrl.workflow.VFlow
import eu.mihosoft.vrl.workflow.VNode
import javafx.beans.property.ObjectProperty
import tornadofx.toProperty
import kotlin.math.max
import kotlin.math.round

/**
 * @author Kevin Ludwig
 */
class GraphVisMutable(
    override var name : String     = "New Graph",
                 meta : Meta = Meta(emptyList()),
                 nodes: List<Node> = emptyList()
) : GraphVis() {
    override val _nodes: List<VNode> get() = flow.nodes
    override val _connections: Map<String, Connections> get() = flow.allConnections

    @get:JsonIgnore lateinit var specLookup: SpecLookup
    @get:JsonIgnore val flow: VFlow by lazy {
        FlowFactory.newFlow().apply {
            isVisible = true

            merge(meta, nodes)
        }
    }

    fun newNode(spec: Spec.Node, meta: Meta.Node?) = flow.newNode(spec, meta, settings)

    fun merge(graph: GraphWithMeta): List<VNode> = flow.merge(graph.meta, graph.nodes)

    private fun VFlow.merge(meta: Meta, nodes: List<Node>): List<VNode> {
        val (embed, other) = nodes.partition { it is Value && it.embed }
        val _embed = embed.associate { (it as Value).out to it.value }
        val _nodes = mutableListOf<VNode>()
        mutableMapOf<Int, MutableList<Connector>>().apply {
            other.forEachIndexed { i, node ->
                val spec = checkNotNull(specLookup.getNodeSpec(node.type))
                val _node = objectMapper.convertValue<Map<String, Any?>>(node)
                _nodes += newNode(spec, meta.nodes.getOrNull(i), settings).apply {
                    (valueObject as NodeValueObject).const.forEach { it.value = _node[it.spec.json] }
                    connectors.forEach {
                        val connectorValueObject = it.valueObject as ConnectorValueObject
                        val connectionId = _node[connectorValueObject.spec.json] as Int
                        if (connectorValueObject.spec.type == Spec.Node.Port.Type.InData) connectorValueObject.value = _embed[connectionId]
                        getOrPut(connectionId) { mutableListOf() } += it
                    }
                    spec.ports.forEach { if (it.multi) objectMapper.convertValue<List<Entry>>(checkNotNull(_node[it.json])).forEach { entry -> getOrPut(entry.value) { mutableListOf() } += checkNotNull(addPort(it, entry.key.toProperty(), null)).apply { if (it.type == Spec.Node.Port.Type.InData) (valueObject as ConnectorValueObject).value = _embed[entry.value] } } }
                }
            }
        }.forEach { (id, connectors) -> connectors.forEach { connectorA -> if (connectorA.isOutput) connectors.forEach { connectorB -> if (connectorB.isInput) connect(connectorA, connectorB).apply { connection.id = id.toString() } } } }
        return _nodes
    }

    companion object {
        fun VFlow.newNode(spec: Spec.Node, meta: Meta.Node?, settings: Settings): VNode = newNode().apply {
            title = spec.name.split('/').last()
            meta?.let {
                x = it.x
                y = it.y
            }

            xProperty().update { if (settings.gridX > 0) x = round(max(it, 0.0) / settings.gridX) * settings.gridX }
            yProperty().update { if (settings.gridY > 0) y = round(max(it, 0.0) / settings.gridY) * settings.gridY }
            widthProperty().update { if (settings.gridX > 0) width = round(it / settings.gridX) * settings.gridX }
            heightProperty().update { if (settings.gridY > 0) height = round(it / settings.gridY) * settings.gridY }

            val const = mutableListOf<NodeValueObject.Const>()
            valueObject = NodeValueObject(spec, const)

            spec.ports.forEach { if (!it.multi) addPort(it, null, const) }
        }

        fun VNode.addPort(spec: Spec.Node.Port, mapKey: ObjectProperty<Any>?, const: MutableList<NodeValueObject.Const>?) = when (spec.type) {
            Spec.Node.Port.Type.InControl -> addInput("control").apply {
                /*visualizationRequest[VisualizationRequest.KEY_CONNECTOR_PREFER_TOP_DOWN] = true*/
                localId = spec.json
                valueObject = ConnectorValueObject(this, spec, mapKey)
            }
            Spec.Node.Port.Type.OutControl -> addOutput("control").apply {
                /*visualizationRequest[VisualizationRequest.KEY_CONNECTOR_PREFER_TOP_DOWN] = true*/
                localId = spec.json
                valueObject = ConnectorValueObject(this, spec, mapKey)
            }
            Spec.Node.Port.Type.InData -> addInput("data").apply {
                localId = spec.json
                valueObject = ConnectorValueObject(this, spec, mapKey)
            }
            Spec.Node.Port.Type.OutData -> addOutput("data").apply {
                localId = spec.json
                valueObject = ConnectorValueObject(this, spec, mapKey)
            }
            Spec.Node.Port.Type.Const -> {
                checkNotNull(const) += NodeValueObject.Const(spec)

                null
            }
        }
    }
}
