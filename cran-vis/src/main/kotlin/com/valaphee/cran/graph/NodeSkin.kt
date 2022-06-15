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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.valaphee.cran.graph.VGraphDefault.Companion.addPort
import com.valaphee.cran.graph.data.JsonSchema
import com.valaphee.cran.graph.data.PropertiesView
import com.valaphee.cran.injector
import com.valaphee.cran.spec.Spec
import com.valaphee.cran.util.asStyleClass
import com.valaphee.cran.util.update
import eu.mihosoft.vrl.workflow.Connector
import eu.mihosoft.vrl.workflow.VFlow
import eu.mihosoft.vrl.workflow.VNode
import eu.mihosoft.vrl.workflow.fx.FXConnectionSkin
import eu.mihosoft.vrl.workflow.fx.FXFlowNodeSkin
import eu.mihosoft.vrl.workflow.fx.FlowNodeWindow
import eu.mihosoft.vrl.workflow.fx.NodeUtil
import javafx.scene.Parent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.action
import tornadofx.add
import tornadofx.button
import tornadofx.clear
import tornadofx.contextmenu
import tornadofx.get
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.item
import tornadofx.label
import tornadofx.onChange
import tornadofx.separator
import tornadofx.textfield
import tornadofx.toProperty
import tornadofx.vbox
import kotlin.math.max

/**
 * @author Kevin Ludwig
 */
class NodeSkin(
    skinFactory: SkinFactory,
    private val parent: Parent,
    node: VNode,
    flow: VFlow
) : FXFlowNodeSkin(skinFactory, parent, node, flow) {
    private var newConnectionSkin: FXConnectionSkin? = null
    private var newConnectionPressEvent: MouseEvent? = null

    private val objectMapper get() = injector.getInstance(ObjectMapper::class.java)

    override fun createNodeWindow(): FlowNodeWindow = super.createNodeWindow().apply window@ {
        // Prevent minimizing, closing and resizing
        leftIcons.clear()
        setShowMinimizeIconCallback { null }
        setShowCloseIconCallback { null }
        isResizableWindow = false
        resizeableWindowProperty().onChange { if (it) isResizableWindow = false }

        model.valueObjectProperty().update { nodeValueObject ->
            (nodeValueObject as? NodeValueObject)?.let {
                nodeValueObject.spec.name.split('/').fold("") { _path, pathElement -> (if (_path.isNotEmpty()) "$_path/$pathElement" else pathElement).also { styleClass += it.asStyleClass() } }
                model.connectors.update { connectors ->
                    contentPane = VBox().apply {
                        clear()


                        nodeValueObject.spec.ports.filter { it.type == Spec.Node.Port.Type.Const }.groupBy { it }.forEach {
                            hbox {
                                label(it.key.name)
                                vbox {
                                    objectMapper.treeToValue<JsonSchema?>(it.key.data)?.let { jsonSchema ->
                                        it.value.forEach {
                                            if (it.type == Spec.Node.Port.Type.Const) add(jsonSchema.toNode(nodeValueObject.const.single { const -> const.spec == it }.valueProperty))
                                            else connectors.filter { connector -> connector.localId == it.json }.forEach { connector ->
                                                val connectorValueObject = connector.valueObject as ConnectorValueObject
                                                connectorValueObject.multiKeyProperty?.let {
                                                    add(hbox {
                                                        val multiKeyProperty = it
                                                        textfield(multiKeyProperty.value.let { objectMapper.writeValueAsString(it) } ?: "") { focusedProperty().onChange { _ -> multiKeyProperty.value = objectMapper.readValue(text) } }
                                                        if (connector.isInput && !model.flow.getConnections(connector.type).isInputConnected(connector)) add(jsonSchema.toNode(connectorValueObject.valueProperty()).apply { hgrow = Priority.ALWAYS })
                                                        button("-") { action { model.removeConnector(connector) } }
                                                    })
                                                } ?: if (connector.isInput && !model.flow.getConnections(connector.type).isInputConnected(connector)) add(jsonSchema.toNode(connectorValueObject.valueProperty())) else Unit
                                            }
                                        }
                                    } ?: it.value.forEach {
                                        connectors.filter { connector -> connector.localId == it.json }.forEach { connector ->
                                            (connector.valueObject as ConnectorValueObject).multiKeyProperty?.let {
                                                add(hbox {
                                                    val multiKeyProperty = it
                                                    textfield(multiKeyProperty.value.let { objectMapper.writeValueAsString(it) } ?: "") {
                                                        hgrow = Priority.ALWAYS
                                                        focusedProperty().onChange { _ -> multiKeyProperty.value = objectMapper.readValue(text) }
                                                    }
                                                    button("-") { action { model.removeConnector(connector) } }
                                                })
                                            }
                                        }
                                    }
                                    if (it.key.multi) add(hbox {
                                        val newMapKeyTextField = textfield { hgrow = Priority.ALWAYS }
                                        button("+") {
                                            action {
                                                checkNotNull(model.addPort(it.key, objectMapper.readValue<Any?>(newMapKeyTextField.text).toProperty(), null))
                                                newMapKeyTextField.clear()
                                            }
                                        }
                                    })
                                }
                            }
                        }
                        hbox {
                            vbox {
                                nodeValueObject.spec.ports.filter { it.type == Spec.Node.Port.Type.InControl || it.type == Spec.Node.Port.Type.InData }.groupBy { it }.forEach {
                                    hbox {
                                        label(it.key.name)
                                        vbox {
                                            objectMapper.treeToValue<JsonSchema?>(it.key.data)?.let { jsonSchema ->
                                                it.value.forEach {
                                                    if (it.type == Spec.Node.Port.Type.Const) add(jsonSchema.toNode(nodeValueObject.const.single { const -> const.spec == it }.valueProperty))
                                                    else connectors.filter { connector -> connector.localId == it.json }.forEach { connector ->
                                                        val connectorValueObject = connector.valueObject as ConnectorValueObject
                                                        connectorValueObject.multiKeyProperty?.let {
                                                            add(hbox {
                                                                val multiKeyProperty = it
                                                                textfield(multiKeyProperty.value.let { objectMapper.writeValueAsString(it) } ?: "") { focusedProperty().onChange { _ -> multiKeyProperty.value = objectMapper.readValue(text) } }
                                                                if (connector.isInput && !model.flow.getConnections(connector.type).isInputConnected(connector)) add(jsonSchema.toNode(connectorValueObject.valueProperty()).apply { hgrow = Priority.ALWAYS })
                                                                button("-") { action { model.removeConnector(connector) } }
                                                            })
                                                        } ?: if (connector.isInput && !model.flow.getConnections(connector.type).isInputConnected(connector)) add(jsonSchema.toNode(connectorValueObject.valueProperty())) else Unit
                                                    }
                                                }
                                            } ?: it.value.forEach {
                                                connectors.filter { connector -> connector.localId == it.json }.forEach { connector ->
                                                    (connector.valueObject as ConnectorValueObject).multiKeyProperty?.let {
                                                        add(hbox {
                                                            val multiKeyProperty = it
                                                            textfield(multiKeyProperty.value.let { objectMapper.writeValueAsString(it) } ?: "") {
                                                                hgrow = Priority.ALWAYS
                                                                focusedProperty().onChange { _ -> multiKeyProperty.value = objectMapper.readValue(text) }
                                                            }
                                                            button("-") { action { model.removeConnector(connector) } }
                                                        })
                                                    }
                                                }
                                            }
                                            if (it.key.multi) add(hbox {
                                                val newMapKeyTextField = textfield { hgrow = Priority.ALWAYS }
                                                button("+") {
                                                    action {
                                                        checkNotNull(model.addPort(it.key, objectMapper.readValue<Any?>(newMapKeyTextField.text).toProperty(), null))
                                                        newMapKeyTextField.clear()
                                                    }
                                                }
                                            })
                                        }
                                    }
                                }
                            }
                            vbox {
                                nodeValueObject.spec.ports.filter { it.type == Spec.Node.Port.Type.OutControl || it.type == Spec.Node.Port.Type.OutData }.groupBy { it }.forEach {
                                    hbox {
                                        label(it.key.name)
                                        vbox {
                                            objectMapper.treeToValue<JsonSchema?>(it.key.data)?.let { jsonSchema ->
                                                it.value.forEach {
                                                    if (it.type == Spec.Node.Port.Type.Const) add(jsonSchema.toNode(nodeValueObject.const.single { const -> const.spec == it }.valueProperty))
                                                    else connectors.filter { connector -> connector.localId == it.json }.forEach { connector ->
                                                        val connectorValueObject = connector.valueObject as ConnectorValueObject
                                                        connectorValueObject.multiKeyProperty?.let {
                                                            add(hbox {
                                                                val multiKeyProperty = it
                                                                textfield(multiKeyProperty.value.let { objectMapper.writeValueAsString(it) } ?: "") { focusedProperty().onChange { _ -> multiKeyProperty.value = objectMapper.readValue(text) } }
                                                                if (connector.isInput && !model.flow.getConnections(connector.type).isInputConnected(connector)) add(jsonSchema.toNode(connectorValueObject.valueProperty()).apply { hgrow = Priority.ALWAYS })
                                                                button("-") { action { model.removeConnector(connector) } }
                                                            })
                                                        } ?: if (connector.isInput && !model.flow.getConnections(connector.type).isInputConnected(connector)) add(jsonSchema.toNode(connectorValueObject.valueProperty())) else Unit
                                                    }
                                                }
                                            } ?: it.value.forEach {
                                                connectors.filter { connector -> connector.localId == it.json }.forEach { connector ->
                                                    (connector.valueObject as ConnectorValueObject).multiKeyProperty?.let {
                                                        add(hbox {
                                                            val multiKeyProperty = it
                                                            textfield(multiKeyProperty.value.let { objectMapper.writeValueAsString(it) } ?: "") {
                                                                hgrow = Priority.ALWAYS
                                                                focusedProperty().onChange { _ -> multiKeyProperty.value = objectMapper.readValue(text) }
                                                            }
                                                            button("-") { action { model.removeConnector(connector) } }
                                                        })
                                                    }
                                                }
                                            }
                                            if (it.key.multi) add(hbox {
                                                val newMapKeyTextField = textfield { hgrow = Priority.ALWAYS }
                                                button("+") {
                                                    action {
                                                        checkNotNull(model.addPort(it.key, objectMapper.readValue<Any?>(newMapKeyTextField.text).toProperty(), null))
                                                        newMapKeyTextField.clear()
                                                    }
                                                }
                                            })
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        contextmenu {
            item((skinFactory as SkinFactory).uiComponent.messages["graph.node.delete"]) { action { controller.remove(model) } }
            separator()
            item((skinFactory as SkinFactory).uiComponent.messages["graph.node.properties"]) { action { (skinFactory as SkinFactory).uiComponent.openInternalWindow(PropertiesView(model)) } }
        }
    }

    override fun addConnector(connector: Connector) {
        super.addConnector(connector)

        val connectorShape = getConnectorShape(connector)

        // Use own new connection skin
        val connectorNode = connectorShape.node
        connectorNode.setOnMousePressed {
            if (!controller.getConnections(connector.type).isInputConnected(connector)) {
                it.consume()
                newConnectionPressEvent = it
            }
        }
        connectorNode.setOnMouseDragged {
            if (!connectorNode.isMouseTransparent) {
                if (!controller.getConnections(connector.type).isInputConnected(connector)) {
                    if (connector.node.flow.getConnections(connector.type).getAllWith(connector).size < connector.maxNumberOfConnections) {
                        it.consume()
                        if (newConnectionSkin == null) {
                            newConnectionSkin = NewConnectionSkin((skinFactory as SkinFactory), parent, connector, controller, connector.type).init()
                            newConnectionSkin!!.add()
                            MouseEvent.fireEvent(newConnectionSkin!!.receiverUI, newConnectionPressEvent!!)
                        }
                        MouseEvent.fireEvent(newConnectionSkin!!.receiverUI, it)
                    }
                }
            }
        }
        connectorNode.setOnMouseReleased {
            if (!connectorNode.isMouseTransparent) {
                connector.click(NodeUtil.mouseBtnFromEvent(it), it)
                if (!controller.getConnections(connector.type).isInputConnected(connector)) {
                    it.consume()
                    try {
                        MouseEvent.fireEvent(newConnectionSkin!!.receiverUI, it)
                    } catch (_: Exception) {
                    }
                    newConnectionSkin = null
                }
            }
        }
    }

    override fun createConnectorShape(connector: Connector) = ConnectorShape(controller, connector)

    fun postInit() = apply { model.connectors.update { model.height = 25.0 + 20.0 * 2 + max(20.0 * model.inputs.size, 20.0 * model.outputs.size) } }
}
