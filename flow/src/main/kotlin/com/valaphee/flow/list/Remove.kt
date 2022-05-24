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
import com.valaphee.flow.Arr
import com.valaphee.flow.DataPath
import com.valaphee.flow.StatelessNode
import com.valaphee.flow.Und
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.Node
import com.valaphee.flow.spec.Out

/**
 * @author Kevin Ludwig
 */
@Node("List/Remove")
class Remove(
    @get:In (""    , Arr) @get:JsonProperty("in_list" ) val inList : DataPath,
    @get:In ("Item", Und) @get:JsonProperty("in_item" ) val inItem : DataPath,
    @get:Out(""    , Arr) @get:JsonProperty("out_list") val outList: DataPath,
) : StatelessNode() {
    override fun initialize() {
        outList.set { inList.getOrThrow<Iterable<Any?>>("in_list") - inItem.get() }
    }
}
