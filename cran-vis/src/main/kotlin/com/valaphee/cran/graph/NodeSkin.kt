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

import com.valaphee.cran.graph.properties.PropertiesView
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
import tornadofx.get
import tornadofx.item
import tornadofx.onChange
import tornadofx.separator
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

    override fun createNodeWindow(): FlowNodeWindow = super.createNodeWindow().apply {
        // Prevent minimizing, closing and resizing
        leftIcons.clear()
        setShowMinimizeIconCallback { null }
        setShowCloseIconCallback { null }
        isResizableWindow = false
        resizeableWindowProperty().onChange { if (it) isResizableWindow = false }

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
                    val numOfExistingConnections = connector.node.flow.getConnections(connector.type).getAllWith(connector).size

                    if (numOfExistingConnections < connector.maxNumberOfConnections) {
                        if (newConnectionSkin == null) {
                            newConnectionSkin = NewConnectionSkin((skinFactory as SkinFactory), parent, connector, controller, connector.type).init()
                            newConnectionSkin!!.add()
                            MouseEvent.fireEvent(newConnectionSkin!!.receiverUI, newConnectionPressEvent!!)
                        }
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

        // Adjust height
        model.height = 25.0 + 20.0 * 2 + max(20.0 * model.inputs.size, 20.0 * model.outputs.size)
    }

    override fun createConnectorShape(connector: Connector) = ConnectorShape(controller, connector)
}
