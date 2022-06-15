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

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject
import com.hazelcast.nio.ObjectDataInput
import com.hazelcast.nio.ObjectDataOutput
import com.hazelcast.nio.serialization.StreamSerializer
import com.valaphee.cran.Scope
import com.valaphee.cran.impl.Implementation
import com.valaphee.cran.injector
import com.valaphee.cran.meta.Meta
import com.valaphee.cran.node.Entry
import com.valaphee.cran.node.Node
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * @author Kevin Ludwig
 */
class GraphDefault(
                              override val name : String    ,
    @get:JsonProperty("meta")          val meta : Meta?     ,
                              override val nodes: List<Node>
) : Graph() {
    private var scope: Scope? = null

    fun run(objectMapper: ObjectMapper, nodeImpls: List<Implementation>, graphLookup: GraphLookup, coroutineDispatcher: CoroutineDispatcher) {
        scope = Scope(objectMapper, nodeImpls, graphLookup, this@GraphDefault, coroutineDispatcher).also { scope ->
            scope.initialize()
            nodes.forEach { if (it is Entry) scope.launch { scope.controlPath(it.out)() } }
        }
    }

    fun shutdown() {
        scope?.cancel()
    }

    class Serializer : StreamSerializer<GraphDefault> {
        @Inject private lateinit var objectMapper: ObjectMapper

        init {
            injector.injectMembers(this)
        }

        override fun getTypeId() = 2

        override fun write(out: ObjectDataOutput, `object`: GraphDefault) {
            objectMapper.writeValue(out, `object`)
        }

        override fun read(`in`: ObjectDataInput): GraphDefault = objectMapper.readValue(`in`, GraphDefault::class.java)
    }
}
