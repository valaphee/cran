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
import javafx.scene.CacheHint
import javafx.scene.layout.Region
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tornadofx.circle
import tornadofx.dynamicContent
import tornadofx.polygon
import tornadofx.toProperty

/**
 * @author Kevin Ludwig
 */
class ConnectorShape(
    private val flow: VFlow,
    connector: Connector
) : Region(), ConnectorShape {
    private var connector: Connector? = null
    private val radiusProperty = 0.0.toProperty()

    init {
        isManaged = true
        isCache = true
        isCacheShape = true
        cacheHint = CacheHint.SPEED

        setConnector(connector)

        // Connector shapes are by default in back of the containing node
        CoroutineScope(Dispatchers.Main).launch { toFront() }
    }

    override fun getConnector() = connector

    override fun setConnector(connector: Connector) {
        this.connector = connector

        dynamicContent(connector.valueObjectProperty()) {
            if (it is ConnectorValueObject) {
                when (connector.type) {
                    "control" -> polygon(
                        -4.0, -4.0,
                         4.0,  0.0,
                        -4.0,  4.0,
                        -4.0, -4.0,
                    ) {
                        styleClass += "node-connector-shape-${connector.type}"
                        if (connector.visualizationRequest.get<Boolean>(VisualizationRequest.KEY_CONNECTOR_PREFER_TOP_DOWN).orElseGet { false }) rotate = 90.0
                    }
                    "data" -> circle(0.0, 0.0, 4.0) { styleClass += "node-connector-shape-${connector.type}" }
                    else -> error(connector.type)
                }
            }
        }
    }

    override fun radiusProperty() = radiusProperty

    override fun setRadius(radius: Double) = Unit

    override fun getRadius() = 0.0

    override fun getNode() = this
}
