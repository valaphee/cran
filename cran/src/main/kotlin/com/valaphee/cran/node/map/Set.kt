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

package com.valaphee.cran.node.map

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.cran.Scope
import com.valaphee.cran.node.Arr
import com.valaphee.cran.node.Node
import com.valaphee.cran.node.Und
import com.valaphee.cran.spec.In
import com.valaphee.cran.spec.NodeSpec
import com.valaphee.cran.spec.Out

/**
 * @author Kevin Ludwig
 */
@NodeSpec("Map/Set")
class Set(
    type: String,
    @get:In (""     , Arr) @get:JsonProperty("in"      ) val `in`   : Int,
    @get:In ("Key"  , Und) @get:JsonProperty("in_key"  ) val inKey  : Int,
    @get:In ("Value", Und) @get:JsonProperty("in_value") val inValue: Int,
    @get:Out(""     , Arr) @get:JsonProperty("out"     ) val out    : Int,
) : Node(type) {
    override fun initialize(scope: Scope) {
        val `in` = scope.dataPath(`in`)
        val inKey = scope.dataPath(inKey)
        val inValue = scope.dataPath(inValue)
        val out = scope.dataPath(out)

        out.set { `in`.getOfType<Map<Any?, Any?>>().also { it + (inKey.get() to inValue.get()) } }
    }
}
