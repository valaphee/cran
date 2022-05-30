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

import com.valaphee.flow.graph.properties.PropertiesView
import eu.mihosoft.vrl.workflow.Connector
import eu.mihosoft.vrl.workflow.VFlow
import eu.mihosoft.vrl.workflow.VNode
import eu.mihosoft.vrl.workflow.fx.FXConnectionSkin
import eu.mihosoft.vrl.workflow.fx.FXFlowNodeSkin
import eu.mihosoft.vrl.workflow.fx.FlowNodeWindow
import eu.mihosoft.vrl.workflow.fx.NodeUtil
import javafx.scene.Parent
import javafx.scene.input.MouseEvent
import tornadofx.action
import tornadofx.contextmenu
import tornadofx.item
import tornadofx.onChange
import tornadofx.separator

/**
 * @author Kevin Ludwig
 */
class NodeSkin(
    skinFactory: SkinFactory,
    private val parent: Parent,
    node: VNode,
    controller: VFlow
) : FXFlowNodeSkin(skinFactory, parent, node, controller) {
    private var newConnectionSkin: FXConnectionSkin? = null
    private var newConnectionPressEvent: MouseEvent? = null

    override fun createNodeWindow(): FlowNodeWindow = super.createNodeWindow().apply {
        // Prevent minimizing, closing and resizing
        leftIcons.clear()
        setShowMinimizeIconCallback { null }
        setShowCloseIconCallback { null }
        isResizableWindow = false
        resizeableWindowProperty().onChange { if (it) isResizableWindow = false }

        contextMenu = contextmenu {
            item("Delete") { action { controller.remove(model) } }
            separator()
            item("Properties") { action { PropertiesView(model).openModal() } }
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
                    val numOfExistingConnections = connector.node.flow.getConnections(connector.type).getAllWith(connector).size

                    if (numOfExistingConnections < connector.maxNumberOfConnections) {
                        if (newConnectionSkin == null) {
                            newConnectionSkin = NewConnectionSkin(skinFactory, parent, connector, controller, connector.type).init()
                            newConnectionSkin!!.add()
                            MouseEvent.fireEvent(newConnectionSkin!!.receiverUI, newConnectionPressEvent!!)
                        }
                        it.consume()
                        MouseEvent.fireEvent(newConnectionSkin!!.receiverUI, it)
                        it.consume()
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
}
