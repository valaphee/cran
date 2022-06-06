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
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.valaphee.cran.graph.ConnectorValueObject
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

            val const = (node.valueObject as NodeValueObject).const
            if (const.isNotEmpty()) fieldset { const.forEach { objectMapper.treeToValue<JsonSchema?>(it.spec.data)?.let { jsonSchema -> field("${it.spec.name} (${it.spec.json})") { inputContainer.add(jsonSchema.toNode(viewModel.bind { it.valueProperty })) } } } }
            val connectors = node.connectors
            if (connectors.isNotEmpty()) fieldset {
                connectors.forEach {
                    if (it.isInput) {
                        val valueObject = it.valueObject as ConnectorValueObject
                        objectMapper.treeToValue<JsonSchema?>(valueObject.spec.data)?.let { jsonSchema -> field("${valueObject.spec.name} (${valueObject.spec.json})") { inputContainer.add(jsonSchema.toNode(viewModel.bind { valueObject.valueProperty() }, node.flow.getConnections(it.type).isInputConnected(it))) } }
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
