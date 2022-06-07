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

package com.valaphee.cran.logic

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.cran.graph.Scope
import com.valaphee.cran.node.Bit
import com.valaphee.cran.node.NodeJvm
import com.valaphee.cran.node.Node
import com.valaphee.cran.spec.In
import com.valaphee.cran.spec.NodeType
import com.valaphee.cran.spec.Out

/**
 * @author Kevin Ludwig
 */
@NodeType("Logic/Not")
class Not(
    type: String,
    @get:In ("X" , Bit) @get:JsonProperty("in" ) val `in`: Int,
    @get:Out("¬X", Bit) @get:JsonProperty("out") val out : Int
) : Node(type), NodeJvm {
    override fun initialize(scope: Scope) {
        val `in` = scope.dataPath(`in`)
        val out = scope.dataPath(out)

        out.set { !`in`.getOfType<Boolean>() }
    }
}
