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

package com.valaphee.flow

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DatabindContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase
import com.valaphee.flow.nesting.SubGraph
import io.github.classgraph.ClassGraph
import kotlin.reflect.full.findAnnotation

/**
 * @author Kevin Ludwig
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonTypeIdResolver(Node.TypeResolver::class)
open class Node {
    class TypeResolver : TypeIdResolverBase() {
        override fun idFromValue(value: Any) = (value as Node).type

        override fun idFromValueAndType(value: Any?, suggestedType: Class<*>) = value?.let { idFromValue(it) } ?: checkNotNull(suggestedType.kotlin.findAnnotation<com.valaphee.flow.spec.NodeType>()).value

        override fun typeFromId(context: DatabindContext, id: String): JavaType = context.constructType(types[id]?.java ?: SubGraph::class.java)

        override fun getMechanism() = JsonTypeInfo.Id.NAME
    }

    @get:JsonProperty("type") open val type = this::class.findAnnotation<com.valaphee.flow.spec.NodeType>()?.value

    open fun initialize(scope: Scope) = Unit

    open fun shutdown() = Unit

    companion object {
        private val types = ClassGraph().enableClassInfo().enableAnnotationInfo().scan().use { it.getClassesWithAnnotation(com.valaphee.flow.spec.NodeType::class.java).map { Class.forName(it.name).kotlin }.associateBy { checkNotNull(it.findAnnotation<com.valaphee.flow.spec.NodeType>()).value } }
    }
}
