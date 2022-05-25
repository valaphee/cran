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

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DatabindContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase
import com.google.common.collect.HashBiMap
import kotlin.reflect.KClass

/**
 * @author Kevin Ludwig
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, property = "type")
@JsonTypeIdResolver(Node.TypeResolver::class)
abstract class Node {
    class TypeResolver : TypeIdResolverBase() {
        override fun idFromValue(value: Any) = checkNotNull(/*value::class.findAnnotation<com.valaphee.flow.spec.Node>()*/types.inverse()[value::class])/*.value*/

        override fun idFromValueAndType(value: Any?, suggestedType: Class<*>) = value?.let { idFromValue(it) } ?: checkNotNull(/*suggestedType.kotlin.findAnnotation<com.valaphee.flow.spec.Node>()*/types.inverse()[suggestedType.kotlin])/*.value*/

        override fun typeFromId(context: DatabindContext, id: String): JavaType = context.constructType(checkNotNull(types[id]).java)

        override fun getMechanism() = JsonTypeInfo.Id.NAME
    }

    open fun initialize() = Unit

    open fun shutdown() = Unit

    companion object {
        val types: HashBiMap<String, KClass<*>> = HashBiMap.create()
    }
}
