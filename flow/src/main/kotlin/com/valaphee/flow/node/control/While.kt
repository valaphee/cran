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

package com.valaphee.flow.node.control

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.node.Bit
import com.valaphee.flow.node.Node
import com.valaphee.flow.Scope
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.NodeType
import com.valaphee.flow.spec.Out

/**
 * @author Kevin Ludwig
 */
@NodeType("Control/While")
class While(
    type: String,
    @get:In (""         ) @get:JsonProperty("in"      ) val `in`   : Int,
    @get:In (""    , Bit) @get:JsonProperty("in_value") val inValue: Int,
    @get:Out("Body"     ) @get:JsonProperty("out_body") val outBody: Int,
    @get:Out("Exit"     ) @get:JsonProperty("out"     ) val out    : Int,
) : Node(type) {
    override fun initialize(scope: Scope) {
        val `in` = scope.controlPath(`in`)
        val inValue = scope.dataPath(inValue)
        val outBody = scope.controlPath(outBody)
        val out = scope.controlPath(out)

        `in`.declare {
            while (inValue.getOrThrow("in_value")) outBody()
            out()
        }
    }
}
