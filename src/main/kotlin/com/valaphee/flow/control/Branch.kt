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

package com.valaphee.flow.control

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.Binding
import com.valaphee.flow.Node

/**
 * @author Kevin Ludwig
 */
class Branch(
    @get:JsonProperty("in") val `in`: Binding,
    @get:JsonProperty("when") val `when`: Map<Any?, Binding>
) : Node() {
    override suspend fun bind() {
        while (true) `when`[`in`.get()]?.set()
    }
}
