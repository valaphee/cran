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

package com.valaphee.cran.node

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DatabindContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase
import com.valaphee.cran.spec.NodeDecl
import io.github.classgraph.ClassGraph
import kotlin.reflect.full.findAnnotation

/**
 * @author Kevin Ludwig
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonTypeIdResolver(Node.TypeResolver::class)
open class Node(
    @get:JsonProperty("type") val type: String,
    @get:JsonAnyGetter val extra: MutableMap<String, Any?> = mutableMapOf()
) {
    object TypeResolver : TypeIdResolverBase() {
        private val types = ClassGraph().enableClassInfo().enableAnnotationInfo().scan().use { it.getClassesWithAnnotation(NodeDecl::class.java).map { Class.forName(it.name).kotlin }.associateBy { checkNotNull(it.findAnnotation<NodeDecl>()).name } }.toMutableMap()

        override fun idFromValue(value: Any) = (value as Node).type

        override fun idFromValueAndType(value: Any?, suggestedType: Class<*>) = value?.let { idFromValue(it) } ?: checkNotNull(suggestedType.kotlin.findAnnotation<NodeDecl>()).name

        override fun typeFromId(context: DatabindContext, id: String): JavaType = context.constructType(types[id]?.java ?: Node::class.java)

        override fun getMechanism() = JsonTypeInfo.Id.NAME
    }

    private val _requirements = mutableMapOf<Int, IntArray>()
    @get:JsonIgnore val requirements get() = _requirements

    infix fun Int.requires(requirement: Int) = requires(intArrayOf(requirement))

    infix fun Int.requires(requirements: IntArray) {
        this@Node._requirements[this] = requirements
    }

    infix fun IntArray.requires(requirement: Int) = requires(intArrayOf(requirement))

    infix fun IntArray.requires(requirements: IntArray) {
        forEach { this@Node._requirements[it] = requirements }
    }


    operator fun get(key: String) = extra[key]

    @JsonAnySetter
    operator fun set(key: String, value: Any?) {
        extra[key] = value
    }

    companion object {
        val nothing = intArrayOf()
    }
}
