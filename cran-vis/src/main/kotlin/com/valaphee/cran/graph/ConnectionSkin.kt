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

import eu.mihosoft.vrl.workflow.Connection
import eu.mihosoft.vrl.workflow.VFlow
import eu.mihosoft.vrl.workflow.VisualizationRequest
import eu.mihosoft.vrl.workflow.fx.DefaultFXConnectionSkin
import javafx.beans.binding.DoubleBinding
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.ContextMenu
import javafx.scene.input.ContextMenuEvent
import javafx.scene.shape.CubicCurveTo
import javafx.scene.shape.MoveTo
import javafx.scene.shape.Path
import tornadofx.action
import tornadofx.get
import tornadofx.item
import tornadofx.separator

/**
 * @author Kevin Ludwig
 */
class ConnectionSkin(
    skinFactory: SkinFactory,
    parent: Parent,
    connection: Connection,
    flow: VFlow,
    type: String
) : DefaultFXConnectionSkin(skinFactory, parent, connection, flow, type) {
    override fun initSenderAndReceiver() {
        super.initSenderAndReceiver()

        receiverConnectorUI.isVisible = false
    }

    override fun initConnnectionPath() {
        // Fix top-down connection path
        val senderNode = senderShape.node
        val senderTopDown = senderShape.connector.visualizationRequest.get<Boolean>(VisualizationRequest.KEY_CONNECTOR_PREFER_TOP_DOWN).orElseGet { false }
        val receiverTopDown = receiverShape.connector.visualizationRequest.get<Boolean>(VisualizationRequest.KEY_CONNECTOR_PREFER_TOP_DOWN).orElseGet { false }
        val startXBinding = object : DoubleBinding() {
            init {
                bind(senderNode.layoutXProperty(), senderNode.translateXProperty(), senderShape.radiusProperty())
            }

            override fun computeValue() = senderNode.layoutX + senderNode.translateX + senderShape.radius
        }
        val startYBinding: DoubleBinding = object : DoubleBinding() {
            init {
                bind(senderNode.layoutYProperty(), senderNode.translateYProperty(), senderShape.radiusProperty())
            }

            override fun computeValue() = senderNode.layoutY + senderNode.translateY + senderShape.radius
        }
        val endXBinding: DoubleBinding = object : DoubleBinding() {
            init {
                bind(receiverConnectorUI.layoutXProperty(), receiverConnectorUI.translateXProperty())
            }

            override fun computeValue() = receiverConnectorUI.layoutX + receiverConnectorUI.translateX
        }
        val endYBinding: DoubleBinding = object : DoubleBinding() {
            init {
                bind(receiverConnectorUI.layoutYProperty(), receiverConnectorUI.translateYProperty())
            }

            override fun computeValue() = receiverConnectorUI.layoutY + receiverConnectorUI.translateY
        }

        connectionPath = Path(MoveTo().apply {
            xProperty().bind(startXBinding)
            yProperty().bind(startYBinding)
        }, CubicCurveTo().apply {
            controlX1Property().bind(object : DoubleBinding() {
                init {
                    bind(startXBinding, endXBinding)
                }

                override fun computeValue() = if (!senderTopDown) (startXBinding.get() + endXBinding.get()) / 2 else startXBinding.get()
            })
            controlY1Property().bind(object : DoubleBinding() {
                init {
                    bind(startYBinding, endYBinding)
                }

                override fun computeValue() = if (senderTopDown) (startYBinding.get() + endYBinding.get()) / 2 else startYBinding.get()
            })
            controlX2Property().bind(object : DoubleBinding() {
                init {
                    bind(startXBinding, endXBinding)
                }

                override fun computeValue() = if (!receiverTopDown) (startXBinding.get() + endXBinding.get()) / 2 else endXBinding.get()
            })
            controlY2Property().bind(object : DoubleBinding() {
                init {
                    bind(startYBinding, endYBinding)
                }

                override fun computeValue() = if (receiverTopDown) (startYBinding.get() + endYBinding.get()) / 2 else endYBinding.get()
            })
            xProperty().bind(endXBinding)
            yProperty().bind(endYBinding)
        })
    }

    override fun initMouseEventHandler() {
        // Fix context menu propagation
        val contextMenu = createContextMenu()
        val contextMenuHandler = EventHandler<ContextMenuEvent> {
            contextMenu.show(it.source as Node, it.screenX, it.screenY)
            it.consume()
        }
        connectionPath.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, contextMenuHandler)
        receiverUI.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, contextMenuHandler)
    }

    override fun createContextMenu() = ContextMenu().apply {
        item((skinFactory as SkinFactory).uiComponent.messages["graph.connection.probe"]) { action {} }
        separator()
        item(skinFactory.uiComponent.messages["graph.connection.remove"]) { action { controller.getConnections(type).remove(model) } }
    }
}
