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

package com.valaphee.cran.node.control

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.cran.node.Node
import com.valaphee.cran.node.Und
import com.valaphee.cran.path.Entry
import com.valaphee.cran.spec.In
import com.valaphee.cran.spec.NodeDecl
import com.valaphee.cran.spec.Out

/**
 * @author Kevin Ludwig
 */
@NodeDecl("Control/Select")
class Select(
    type: String,
    @get:In (""       , Und      ) @get:JsonProperty("in"        ) val `in`     : Int                      ,
    @get:In (""       , Und, true) @get:JsonProperty("in_value"  ) val inValue  : List<Entry> = emptyList(),
    @get:In ("Default", Und      ) @get:JsonProperty("in_default") val inDefault: Int                      ,
    @get:Out(""       , Und      ) @get:JsonProperty("out"       ) val out      : Int
) : Node(type) {
    init {
        out requires (inValue.map { it.value } + `in` + inDefault).toIntArray()
    }
}
