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

package com.valaphee.cran.spec

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject
import com.hazelcast.nio.ObjectDataInput
import com.hazelcast.nio.ObjectDataOutput
import com.hazelcast.nio.serialization.StreamSerializer
import com.valaphee.cran.injector

/**
 * @author Kevin Ludwig
 */
class SpecNodeSerializer : StreamSerializer<Spec.Node> {
    @Inject private lateinit var objectMapper: ObjectMapper

    init {
        injector.injectMembers(this)
    }

    override fun getTypeId() = 1

    override fun write(out: ObjectDataOutput, `object`: Spec.Node) {
        objectMapper.writeValue(out, `object`)
    }

    override fun read(`in`: ObjectDataInput): Spec.Node = objectMapper.readValue(`in`, Spec.Node::class.java)
}
