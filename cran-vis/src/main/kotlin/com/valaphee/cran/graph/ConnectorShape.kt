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

import eu.mihosoft.vrl.workflow.Connector
import eu.mihosoft.vrl.workflow.VFlow
import eu.mihosoft.vrl.workflow.VisualizationRequest
import eu.mihosoft.vrl.workflow.fx.ConnectorShape
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.text.Text
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tornadofx.circle
import tornadofx.doubleBinding
import tornadofx.dynamicContent
import tornadofx.label
import tornadofx.paddingLeft
import tornadofx.paddingRight
import tornadofx.polygon
import tornadofx.toProperty

/**
 * @author Kevin Ludwig
 */
class ConnectorShape(
    private val flow: VFlow,
    connector: Connector
) : HBox(), ConnectorShape {
    private var connector: Connector? = null
    private val radiusProperty = 0.0.toProperty()

    init {
        setConnector(connector)

        // Connector shapes are by default in back of the containing node
        CoroutineScope(Dispatchers.Main).launch { toFront() }
    }

    override fun getConnector() = connector

    override fun setConnector(connector: Connector) {
        this.connector = connector

        dynamicContent(connector.valueObjectProperty()) {
            children.clear()

            val nodeValueObject = (connector.node.valueObject as NodeValueObject)
            if (it is ConnectorValueObject) {
                spacing = 4.0

                val topDown = connector.visualizationRequest.get<Boolean>(VisualizationRequest.KEY_CONNECTOR_PREFER_TOP_DOWN).orElseGet { false }
                if (connector.isInput) {
                    paddingLeft = -4.0
                    alignment = Pos.CENTER_LEFT

                    when (connector.type) {
                        "control" -> polygon(
                            0.0, -4.0,
                            8.0,  0.0,
                            0.0,  4.0,
                            0.0, -4.0,
                        ) {
                            styleClass += "node-connector-shape-${connector.type}"
                            if (topDown) rotate = 90.0
                        }
                        "data" -> circle(4.0, 0.0, 4.0) { styleClass += "node-connector-shape-${connector.type}" }
                        else -> error(connector.type)
                    }
                    label(when (nodeValueObject.spec.name) {
                        "Nesting/Control Output", "Nesting/Data Output" -> nodeValueObject.const.single { it.spec.json == "name" }.valueProperty.asString()
                        else -> it.multiKeyProperty?.asString() ?: it.spec.name.toProperty()
                    }) {
                        minWidthProperty().bind(Text().let {
                            it.textProperty().bind(textProperty())
                            it.layoutBoundsProperty().doubleBinding { it?.width ?: 0.0 }
                        })
                        textFill = Color.WHITE
                    }
                } else {
                    paddingRight = -4.0
                    alignment = Pos.CENTER_RIGHT

                    label(when (nodeValueObject.spec.name) {
                        "Nesting/Control Input", "Nesting/Data Input" -> nodeValueObject.const.single { it.spec.json == "name" }.valueProperty.asString()
                        else -> it.multiKeyProperty?.asString() ?: it.spec.name.toProperty()
                    }) {
                        minWidthProperty().bind(Text().let {
                            it.textProperty().bind(textProperty())
                            it.layoutBoundsProperty().doubleBinding { it?.width ?: 0.0 }
                        })
                        textFill = Color.WHITE
                    }
                    when (connector.type) {
                        "control" -> polygon(
                            0.0, -4.0,
                            8.0,  0.0,
                            0.0,  4.0,
                            0.0, -4.0
                        ) {
                            styleClass += "node-connector-shape-${connector.type}"
                            if (topDown) rotate = 90.0
                        }
                        "data" -> circle(4.0, 0.0, 4.0) { styleClass += "node-connector-shape-${connector.type}" }
                        else -> error(connector.type)
                    }
                }
            }
        }
    }

    override fun radiusProperty() = radiusProperty

    override fun setRadius(radius: Double) = Unit

    override fun getRadius() = 0.0

    override fun getNode() = this
}
