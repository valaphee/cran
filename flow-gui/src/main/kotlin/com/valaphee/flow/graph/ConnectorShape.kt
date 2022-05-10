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
import eu.mihosoft.vrl.workflow.fx.ConnectorShape
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.text.Text
import tornadofx.circle
import tornadofx.label
import tornadofx.paddingLeft
import tornadofx.paddingTop
import tornadofx.polygon
import tornadofx.toProperty

/**
 * @author Kevin Ludwig
 */
class ConnectorShape(
    connector: Connector
) : Region(), ConnectorShape {
    private var connector: Connector? = null
    private val radiusProperty = 0.0.toProperty()

    init {
        setConnector(connector)

        when (connector.type) {
            "control" -> polygon(
                0.0 + if (connector.isInput) -2.0 else -4.0, -4.0,
                8.0 + if (connector.isInput) -2.0 else -4.0,  0.0,
                0.0 + if (connector.isInput) -2.0 else -4.0,  4.0,
                0.0 + if (connector.isInput) -2.0 else -4.0, -4.0
            ) { fill = Color.WHITE }
            "data" -> circle(4.0 + if (connector.isInput) -2.0 else -4.0, 0.0, 4.0) { fill = Color.WHITE }
        }
        val (spec, _) = @Suppress("UNCHECKED_CAST") (connector.valueObject.value as Pair<Spec.Node.Port, Any?>)
        label(spec.name) {
            paddingLeft = if (connector.isInput) 8.0 else -Text(text).layoutBounds.width - 8.0
            paddingTop = -9.0
            textFill = Color.WHITE
        }
    }

    override fun getConnector() = connector

    override fun setConnector(connector: Connector) {
        this.connector = connector
    }

    override fun radiusProperty() = radiusProperty

    override fun setRadius(radius: Double) = Unit

    override fun getRadius() = 0.0

    override fun getNode() = this
}
