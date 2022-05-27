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

package com.valaphee.flow.graph.properties

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.valaphee.flow.graph.ConnectorValueObject
import com.valaphee.flow.graph.NodeValueObject
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
import tornadofx.vbox
import tornadofx.vgrow

/**
 * @author Kevin Ludwig
 */
class PropertiesView(
    private val node: VNode
) : View("Properties - ${(node.valueObject as NodeValueObject).spec.name}") {
    private val objectMapper by di<ObjectMapper>()

    private val viewModel = ViewModel()

    override val root = vbox {
        prefWidth = 400.0
        styleClass += "background"

        form {
            vgrow = Priority.ALWAYS

            fieldset {
                node.connectors.forEach { connector ->
                    if (connector.isInput) {
                        val valueObject = connector.valueObject as ConnectorValueObject
                        objectMapper.treeToValue<JsonSchema?>(valueObject.spec.data)?.let { jsonSchema -> field("${valueObject.spec.name} (${valueObject.spec.json})") { inputContainer.add(jsonSchema.toNode(viewModel.bind { valueObject.valueProperty() })) } }
                    }
                }
            }
        }
        buttonbar {
            button("Ok") {
                action {
                    viewModel.commit()
                    close()
                }
            }
            button("Cancel") { action { close() } }
            button("Apply") {
                enableWhen(viewModel.dirty)
                action { viewModel.commit() }
            }
        }
    }
}
