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

package com.valaphee.flow.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.Binding
import com.valaphee.flow.LazyNode

/**
 * @author Kevin Ludwig
 */
class ListGet(
    @get:JsonProperty("in_list") val inList: Binding,
    @get:JsonProperty("in_index") val inIndex: Binding,
    @get:JsonProperty("out") val out: Binding
) : LazyNode() {
    override suspend fun bind() {
        out.set {
            @Suppress("UNCHECKED_CAST")
            (inList.get() as MutableList<Any?>)[inIndex.get() as Int]
        }
    }
}
