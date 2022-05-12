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

package com.valaphee.flow.list

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.DataPath
import com.valaphee.flow.StatelessNode
import com.valaphee.flow.getOrThrow
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.Node
import com.valaphee.flow.spec.Out

/**
 * @author Kevin Ludwig
 */
@Node("List/Get")
class Get(
    @get:In           @get:JsonProperty("in_list" ) val inList : DataPath,
    @get:In ("Index") @get:JsonProperty("in_index") val inIndex: DataPath,
    @get:Out          @get:JsonProperty("out"     ) val out    : DataPath
) : StatelessNode() {
    override fun initialize() {
        out.set { inList.getOrThrow<List<Any?>>()[inIndex.get() as Int] }
    }
}
