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

package com.valaphee.cran.node.list

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.cran.Scope
import com.valaphee.cran.node.Arr
import com.valaphee.cran.node.Int
import com.valaphee.cran.node.Node
import com.valaphee.cran.node.Und
import com.valaphee.cran.spec.In
import com.valaphee.cran.spec.NodeSpec
import com.valaphee.cran.spec.Out

/**
 * @author Kevin Ludwig
 */
@NodeSpec("List/Get")
class Get(
    type: String,
    @get:In (""     , Arr) @get:JsonProperty("in"      ) val `in`   : Int,
    @get:In ("Index", Int) @get:JsonProperty("in_index") val inIndex: Int,
    @get:Out(""     , Und) @get:JsonProperty("out"     ) val out    : Int
) : Node(type) {
    override fun initialize(scope: Scope) {
        val `in` = scope.dataPath(`in`)
        val inIndex = scope.dataPath(inIndex)
        val out = scope.dataPath(out)

        out.set { `in`.getOfType<List<Any?>>()[inIndex.getOfType()] }
    }
}
