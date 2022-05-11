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
import com.valaphee.flow.LazyNode
import com.valaphee.flow.getOrThrow
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.Node
import com.valaphee.flow.spec.Out
import kotlinx.coroutines.CoroutineScope

/**
 * @author Kevin Ludwig
 */
@Node("List/Remove")
class Remove(
    @get:In          @get:JsonProperty("in_list")  val inList : DataPath,
    @get:In ("Item") @get:JsonProperty("in_item")  val inItem : DataPath,
    @get:Out         @get:JsonProperty("out_list") val outList: DataPath,
) : LazyNode() {
    override fun initialize(scope: CoroutineScope) {
        outList.set { inList.getOrThrow<List<Any?>>() - inItem.get() }
    }
}