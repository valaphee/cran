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

import eu.mihosoft.vrl.workflow.Connector
import eu.mihosoft.vrl.workflow.fx.ConnectorShape
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tornadofx.circle
import tornadofx.paddingLeft
import tornadofx.paddingRight
import tornadofx.polygon
import tornadofx.toProperty

/**
 * @author Kevin Ludwig
 */
class ConnectorShape(
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
        if (connector.isInput) {
            // Properties
            paddingLeft = -4.0
            alignment = Pos.CENTER_LEFT

            // Children
            when (connector.type) {
                "control" -> polygon(
                    0.0, -4.0,
                    8.0,  0.0,
                    0.0,  4.0,
                    0.0, -4.0
                ) { fill = Color.WHITE }
                "data"/*, "const"*/ -> circle(4.0, 0.0, 4.0) { fill = Color.WHITE }
                "const" -> Unit
                else -> error(connector.type)
            }
        } else {
            // Properties
            paddingRight = -4.0
            alignment = Pos.CENTER_RIGHT

            // Children
            when (connector.type) {
                "control" -> polygon(
                    0.0, -4.0,
                    8.0,  0.0,
                    0.0,  4.0,
                    0.0, -4.0
                ) { fill = Color.WHITE }
                "data" -> circle(4.0, 0.0, 4.0) { fill = Color.WHITE }
                else -> error(connector.type)
            }
        }
    }

    override fun radiusProperty() = radiusProperty

    override fun setRadius(radius: Double) = Unit

    override fun getRadius() = 0.0

    override fun getNode() = this
}
