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
import com.valaphee.cran.graph.Scope
import com.valaphee.cran.node.Node
import com.valaphee.cran.node.NodeJvm
import com.valaphee.cran.node.Und
import com.valaphee.cran.spec.In
import com.valaphee.cran.spec.NodeSpec
import com.valaphee.cran.spec.Out

/**
 * @author Kevin Ludwig
 */
@NodeSpec("Control/Branch")
class Branch(
    type: String,
    @get:In (""            ) @get:JsonProperty("in"         ) val `in`      : Int           ,
    @get:In (""       , Und) @get:JsonProperty("in_value"   ) val inValue   : Int           ,
    @get:Out(""            ) @get:JsonProperty("out"        ) val out       : Map<Any?, Int>,
    @get:Out("Default"     ) @get:JsonProperty("out_default") val outDefault: Int
) : Node(type), NodeJvm {
    override fun initialize(scope: Scope) {
        val `in` = scope.controlPath(`in`)
        val inValue = scope.dataPath(inValue)
        val out = out.mapValues { scope.controlPath(it.value) }
        val outDefault = scope.controlPath(outDefault)

        `in`.declare { (out[inValue.get()] ?: outDefault)() }
    }
}
