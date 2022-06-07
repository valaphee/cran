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

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Inject
import javafx.beans.property.ObjectProperty
import javafx.scene.Node
import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
import tornadofx.onChange

/**
 * @author Kevin Ludwig
 */
class JsonSchema(
    @get:JsonProperty("type"    ) val type    : Type?      ,
    @get:JsonProperty("items"   ) val items   : JsonSchema?,
    @get:JsonProperty("minItems") val minItems: Int?       ,
    @get:JsonProperty("maxItems") val maxItems: Int?
) {
    enum class Type {
        @JsonProperty("boolean") Boolean,
        @JsonProperty("integer") Integer,
        @JsonProperty("number" ) Number ,
        @JsonProperty("string" ) String ,
        @JsonProperty("array"  ) Array
    }

    @Inject private lateinit var objectMapper: ObjectMapper

    fun toNode(valueProperty: ObjectProperty<Any?>, readOnly: Boolean = false): Node {
        lateinit var node: Node
        node = toNode(valueProperty.value, readOnly) { toValue(node)?.let { valueProperty.value = it } }
        valueProperty.onChange { updateNode(it, node) }
        return node
    }

    private fun toNode(value: Any?, readOnly: Boolean, toValue: () -> Unit) = when (type) {
        Type.Boolean -> CheckBox().apply {
            isSelected = value as Boolean? ?: false
            selectedProperty().onChange { toValue() }
            isDisable = readOnly
        }
        Type.Integer -> TextField().apply {
            isDisable = readOnly
            text = (value as Number?)?.toString() ?: "0"
            textProperty().onChange { toValue() }
        }
        Type.Number -> TextField().apply {
            isDisable = readOnly
            text = (value as Number?)?.toString() ?: "0.0"
            textProperty().onChange { toValue() }
        }
        Type.String -> TextField().apply {
            isDisable = readOnly
            text = value?.toString() ?: ""
            textProperty().onChange { toValue() }
        }
        else -> TextField().apply {
            isDisable = readOnly
            text = value?.let { objectMapper.writeValueAsString(it) } ?: ""
            focusedProperty().onChange { toValue() }
        }
    }

    private fun updateNode(value: Any?, node: Node) {
        when (type) {
            Type.Boolean -> (node as CheckBox).isSelected = value as Boolean? ?: false
            Type.Integer -> (node as TextField).text = (value as Number?)?.toString() ?: "0.0"
            Type.Number  -> (node as TextField).text = (value as Number?)?.toString() ?: "0"
            Type.String  -> (node as TextField).text = value?.toString() ?: ""
            else         -> (node as TextField).text = value?.let { objectMapper.writeValueAsString(it) } ?: ""
        }
    }

    private fun toValue(node: Node) = when (type) {
        Type.Boolean -> (node as CheckBox).isSelected
        Type.Integer -> (node as TextField).text?.toIntOrNull()
        Type.Number  -> (node as TextField).text?.toDoubleOrNull()
        Type.String  -> (node as TextField).text
        else         -> try {
            objectMapper.readValue((node as TextField).text)
        } catch (_: JsonProcessingException) {
            null
        }
    }
}
