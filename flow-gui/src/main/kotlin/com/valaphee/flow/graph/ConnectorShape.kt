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

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.valaphee.flow.spec.Spec
import com.valaphee.flow.spec.DataType
import eu.mihosoft.vrl.workflow.ConnectionEvent
import eu.mihosoft.vrl.workflow.Connector
import eu.mihosoft.vrl.workflow.VFlow
import eu.mihosoft.vrl.workflow.fx.ConnectorShape
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.text.Text
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tornadofx.checkbox
import tornadofx.circle
import tornadofx.label
import tornadofx.onChange
import tornadofx.paddingLeft
import tornadofx.paddingRight
import tornadofx.polygon
import tornadofx.textfield
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

        fun content(value: Pair<Spec.Node.Port, Any?>?) {
            children.clear()

            value?.let { (spec, const) ->
                spacing = 4.0

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
                    label(spec.name) {
                        minWidth = Text(text).layoutBounds.width
                        textFill = Color.WHITE
                    }
                    if (connector.type == "data" || connector.type == "const") when (spec.dataType) {
                        DataType.Bin -> checkbox(null, (const as? Boolean ?: false).toProperty().apply {
                            onChange {
                                try {
                                    connector.valueObject.value = spec to it
                                } catch (_: Throwable) {
                                }
                            }
                        })
                        else -> {
                            val objectMapper = jacksonObjectMapper()
                            textfield(const?.let { objectMapper.writeValueAsString(it) } ?: "") {
                                // Properties
                                minWidth = 100.0

                                // Events
                                focusedProperty().onChange {
                                    if (!it) connector.valueObject.value = spec to try {
                                        if (text.isBlank()) null else objectMapper.readValue<Any?>(text)
                                    } catch (_: Throwable) {
                                        text
                                    }
                                }
                            }
                        }
                    }.apply {
                        isVisible = !flow.getConnections(connector.type).isInputConnected(connector) || connector.type == "const"
                        connector.addConnectionEventListener {
                            when (it.eventType) {
                                ConnectionEvent.ADD -> isVisible = false
                                ConnectionEvent.REMOVE -> isVisible = true
                            }
                        }
                    } else Unit
                } else {
                    // Properties
                    paddingRight = -4.0
                    alignment = Pos.CENTER_RIGHT

                    // Children
                    label(spec.name) {
                        minWidth = Text(text).layoutBounds.width
                        textFill = Color.WHITE
                    }
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
        }

        content(@Suppress("UNCHECKED_CAST") (connector.valueObject.value as Pair<Spec.Node.Port, Any?>?))
        connector.valueObject.valueProperty().onChange { content(@Suppress("UNCHECKED_CAST") (it as Pair<Spec.Node.Port, Any?>?)) }
    }

    override fun radiusProperty() = radiusProperty

    override fun setRadius(radius: Double) = Unit

    override fun getRadius() = 0.0

    override fun getNode() = this
}
