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

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Inject
import javafx.beans.property.ObjectProperty
import javafx.scene.Node
import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import tornadofx.hgrow
import tornadofx.onChange

/**
 * @author Kevin Ludwig
 */
class JsonSchema(
    @get:JsonProperty("type"    ) val type    : Type?    ,
    @get:JsonProperty("items"   ) val items   : JsonSchema?,
    @get:JsonProperty("minItems") val minItems: Int?       ,
    @get:JsonProperty("maxItems") val maxItems: Int?
) {
    enum class Type {
        @JsonProperty("boolean") Boolean,
        @JsonProperty("number" ) Number ,
        @JsonProperty("string" ) String ,
        @JsonProperty("array"  ) Array
    }

    @Inject private lateinit var objectMapper: ObjectMapper

    fun toNode(valueProperty: ObjectProperty<Any?>): Node {
        lateinit var node: Node
        node = toNode(valueProperty.value) { valueProperty.value = toValue(node) }
        valueProperty.onChange { updateNode(it, node) }
        return node
    }

    private fun toNode(value: Any?, toValue: () -> Unit): Node = when (type) {
        Type.Boolean -> CheckBox().apply {
            isSelected = value as Boolean? ?: false
            selectedProperty().onChange { toValue() }
        }
        Type.Number -> TextField().apply {
            text = (value as Number?)?.toString()
            textProperty().onChange { toValue() }
        }
        Type.String -> TextField().apply {
            text = value?.toString()
            textProperty().onChange { toValue() }
        }
        Type.Array -> HBox().apply {
            check(items != null && minItems != null && maxItems != null)
            val _value = value as List<*>?
            repeat(minItems) { children += items.toNode(_value?.get(it) ?: 0, toValue).apply { hgrow = Priority.ALWAYS } }
        }
        else -> TextField().apply {
            text = objectMapper.writeValueAsString(value)
            focusedProperty().onChange { toValue() }
        }
    }

    private fun updateNode(value: Any?, node: Node) {
        when (type) {
            Type.Boolean -> (node as CheckBox).isSelected = value as Boolean? ?: false
            Type.Number -> (node as TextField).text = (value as Number?)?.toString() ?: "0"
            Type.String -> (node as TextField).text = value?.toString() ?: ""
            Type.Array -> {
                check(items != null && minItems != null && maxItems != null)
                val _value = value as List<*>?
                (node as HBox).children.forEachIndexed { i, child -> items.updateNode(_value?.get(i) ?: 0, child) }
            }
            else -> (node as TextField).text = objectMapper.writeValueAsString(value)
        }
    }

    private fun toValue(node: Node): Any? = when (type) {
        Type.Boolean -> (node as CheckBox).isSelected
        Type.Number -> (node as TextField).text?.toDouble() ?: 0.0
        Type.String -> (node as TextField).text ?: ""
        Type.Array -> {
            check(items != null && minItems != null && maxItems != null)
            (node as HBox).children.map { items.toValue(it) }
        }
        else -> objectMapper.readValue((node as TextField).text)
    }
}
