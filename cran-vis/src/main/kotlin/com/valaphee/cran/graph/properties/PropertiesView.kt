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

package com.valaphee.cran.graph.properties

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.valaphee.cran.graph.ConnectorValueObject
import com.valaphee.cran.graph.GraphImpl.Companion.addPort
import com.valaphee.cran.graph.NodeValueObject
import eu.mihosoft.vrl.workflow.VNode
import javafx.scene.layout.Priority
import tornadofx.View
import tornadofx.ViewModel
import tornadofx.action
import tornadofx.button
import tornadofx.buttonbar
import tornadofx.enableWhen
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.format
import tornadofx.get
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.onChange
import tornadofx.textfield
import tornadofx.toProperty
import tornadofx.vbox
import tornadofx.vgrow

/**
 * @author Kevin Ludwig
 */
class PropertiesView(
    private val node: VNode
) : View("%graph.properties") {
    private val objectMapper by di<ObjectMapper>()

    override val root = vbox {
        prefWidth = 400.0

        styleClass += "background"
        stylesheets += "/dark_theme.css"

        val viewModel = ViewModel()

        form {
            vgrow = Priority.ALWAYS

            val nodeValueObject = (node.valueObject as NodeValueObject)
            nodeValueObject.spec.ports.groupBy { it.type }.forEach {
                fieldset(it.key.name) {

                    it.value.groupBy { it }.forEach {
                        field("${it.key.name} (${it.key.json})") {
                            vbox {
                                objectMapper.treeToValue<JsonSchema?>(it.key.data)?.let { jsonSchema ->
                                    it.value.forEach {
                                        node.connectors.filter { connector -> connector.localId == it.json }.forEach { connector ->
                                            val connectorValueObject = connector.valueObject as ConnectorValueObject
                                            connectorValueObject.multiKeyProperty?.let {
                                                add(hbox {
                                                    val multiKeyProperty = viewModel.bind { it }
                                                    textfield(multiKeyProperty.value.let { objectMapper.writeValueAsString(it) } ?: "") { focusedProperty().onChange { _ -> multiKeyProperty.value = objectMapper.readValue(text) } }
                                                    add(jsonSchema.toNode(viewModel.bind { connectorValueObject.valueProperty() }, node.flow.getConnections(connector.type).isInputConnected(connector)).apply { hgrow = Priority.ALWAYS })
                                                    button("-") { action { node.removeConnector(connector) } }
                                                })
                                            } ?: add(jsonSchema.toNode(viewModel.bind { connectorValueObject.valueProperty() }, node.flow.getConnections(connector.type).isInputConnected(connector)))
                                        }
                                    }
                                } ?: it.value.forEach {
                                    node.connectors.filter { connector -> connector.localId == it.json }.forEach { connector ->
                                        (connector.valueObject as ConnectorValueObject).multiKeyProperty?.let {
                                            add(hbox {
                                                val multiKeyProperty = viewModel.bind { it }
                                                textfield(multiKeyProperty.value.let { objectMapper.writeValueAsString(it) } ?: "") {
                                                    hgrow = Priority.ALWAYS
                                                    focusedProperty().onChange { _ -> multiKeyProperty.value = objectMapper.readValue(text) }
                                                }
                                                button("-") { action { node.removeConnector(connector) } }
                                            })
                                        }
                                    }
                                }
                                if (it.key.multi) add(hbox {
                                    val newMapKeyTextField = textfield { hgrow = Priority.ALWAYS }
                                    button("+") {
                                        action {
                                            checkNotNull(node.addPort(it.key, objectMapper.readValue<Any?>(newMapKeyTextField.text).toProperty(), null))
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
        buttonbar {
            button(messages["graph.properties.ok"]) {
                isDefaultButton = true

                action {
                    viewModel.commit()
                    close()
                }
            }
            button(messages["graph.properties.cancel"]) {
                isCancelButton = true

                action { close() }
            }
            button(messages["graph.properties.apply"]) {
                enableWhen(viewModel.dirty)

                action { viewModel.commit() }
            }
        }
    }

    init {
        title = messages.format("graph.properties", (node.valueObject as NodeValueObject).spec.name)
    }
}
