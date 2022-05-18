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

import com.valaphee.flow.spec.Spec
import eu.mihosoft.vrl.workflow.Connector
import eu.mihosoft.vrl.workflow.VFlow
import eu.mihosoft.vrl.workflow.VNode
import eu.mihosoft.vrl.workflow.fx.FXFlowNodeSkin
import eu.mihosoft.vrl.workflow.fx.FlowNodeWindow
import javafx.scene.Parent
import javafx.scene.image.Image
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundImage
import javafx.scene.layout.BackgroundPosition
import javafx.scene.layout.BackgroundRepeat
import javafx.scene.layout.BackgroundSize
import jfxtras.scene.control.window.WindowIcon
import tornadofx.onChange

/**
 * @author Kevin Ludwig
 */
class NodeSkin(
    skinFactory: SkinFactory,
    parent: Parent,
    node: VNode,
    controller: VFlow
) : FXFlowNodeSkin(skinFactory, parent, node, controller) {
    override fun createNodeWindow(): FlowNodeWindow = super.createNodeWindow().apply {
        // Prevent minimizing, closing and resizing
        leftIcons.clear()
        setShowMinimizeIconCallback { null }
        setShowCloseIconCallback { null }
        isResizableWindow = false
        resizeableWindowProperty().onChange { if (it) isResizableWindow = false }

        // Icon
        fun icon(spec: Spec.Node?) {
            leftIcons.clear()

            spec?.let { (skinFactory as SkinFactory).manifest.nodes[it.name]?.let { this::class.java.getResourceAsStream(it.icon)?.let { leftIcons += WindowIcon().apply { background = Background(BackgroundImage(Image(it, 32.0, 32.0, false, false), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, true, true, false, true))) } } } }
        }

        icon(@Suppress("UNCHECKED_CAST") (model.valueObject.value as Spec.Node?))
        model.valueObject.valueProperty().onChange { icon(@Suppress("UNCHECKED_CAST") (it as Spec.Node?)) }
    }

    override fun addConnector(connector: Connector) {
        super.addConnector(connector)

        // Prevent connecting
        if (connector.type == "const") {
            val connectorShape = getConnectorShape(connector)
            connectorShape.node.setOnMousePressed { }
            connectorShape.node.setOnMouseDragged { }
            connectorShape.node.setOnMouseReleased { }
        }
    }

    override fun createConnectorShape(connector: Connector) = ConnectorShape(connector)
}
