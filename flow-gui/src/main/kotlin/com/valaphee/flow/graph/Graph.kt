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

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.spec.Spec
import eu.mihosoft.vrl.workflow.FlowFactory
import eu.mihosoft.vrl.workflow.incubating.LayoutGeneratorSmart
import javafx.scene.Parent
import java.util.UUID

/**
 * @author Kevin Ludwig
 */
class Graph(
    @get:JsonProperty("id"   ) val id   : UUID = UUID.randomUUID(),
    @get:JsonProperty("graph") val graph: List<MutableMap<String, Any?>>
) {
    fun flow(parent: Parent, spec: Spec) {
        FlowFactory.newFlow().apply {
            isVisible = true

            val connectors = graph.map {
                val type = it.remove("type") as String
                val nodeSpec = spec.nodes.single { it.json == type }
                val node = newNode().apply { title = nodeSpec.name }
                nodeSpec.ports.mapNotNull { nodePortSpec ->
                    when (nodePortSpec.type) {
                        Spec.Node.Port.Type.InControl -> it[nodePortSpec.json] as Int to node.addInput("control").apply {
                            /*visualizationRequest[VisualizationRequest.KEY_CONNECTOR_PREFER_TOP_DOWN] = true*/
                            localId = nodePortSpec.json
                            valueObject.value = nodePortSpec to null
                        }
                        Spec.Node.Port.Type.OutControl -> it[nodePortSpec.json] as Int to node.addOutput("control").apply {
                            /*visualizationRequest[VisualizationRequest.KEY_CONNECTOR_PREFER_TOP_DOWN] = true*/
                            localId = nodePortSpec.json
                            valueObject.value = nodePortSpec to null
                        }
                        Spec.Node.Port.Type.InData -> it[nodePortSpec.json] as Int to node.addInput("data").apply {
                            localId = nodePortSpec.json
                            valueObject.value = nodePortSpec to null
                        }
                        Spec.Node.Port.Type.OutData -> it[nodePortSpec.json] as Int to node.addOutput("data").apply {
                            localId = nodePortSpec.json
                            valueObject.value = nodePortSpec to null
                        }
                        Spec.Node.Port.Type.Const -> {
                            node.addInput("const").apply {
                                localId = nodePortSpec.json
                                valueObject.value = nodePortSpec to it[nodePortSpec.json]
                            }
                            null
                        }
                    }
                }.toMap()
            }
            graph.zip(connectors).forEach { it.first.entries.forEach { port -> it.second[port.value]?.let { connectorA -> connectors.forEach { it[port.value]?.let { connectorB -> connect(connectorA, connectorB) } } } } }

            LayoutGeneratorSmart().apply { workflow = model }.generateLayout()
        }.setSkinFactories(SkinFactory(parent))
    }
}
