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
import com.valaphee.flow.Scope
import com.valaphee.flow.node.Node
import com.valaphee.flow.node.Num
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.NodeType
import com.valaphee.flow.spec.Out

/**
 * @author Kevin Ludwig
 */
@NodeType("Control/For")
class For(
    type: String,
    @get:In (""          ) @get:JsonProperty("in"            ) val `in`        : Int,
    @get:In ("Start", Num) @get:JsonProperty("in_range_start") val inRangeStart: Int,
    @get:In ("End"  , Num) @get:JsonProperty("in_range_end"  ) val inRangeEnd  : Int,
    @get:In ("Step" , Num) @get:JsonProperty("in_step"       ) val inStep      : Int,
    @get:Out("Body"      ) @get:JsonProperty("out_body"      ) val outBody     : Int,
    @get:Out("Exit"      ) @get:JsonProperty("out"           ) val out         : Int,
    @get:Out("Index", Num) @get:JsonProperty("out_index"     ) val outIndex    : Int
) : Node(type) {
    override fun initialize(scope: Scope) {
        val `in` = scope.controlPath(`in`)
        val inRangeStart = scope.dataPath(inRangeStart)
        val inRangeEnd = scope.dataPath(inRangeEnd)
        val inStep = scope.dataPath(inStep)
        val outBody = scope.controlPath(outBody)
        val out = scope.controlPath(out)
        val outIndex = scope.dataPath(outIndex)

        `in`.declare {
            IntProgression.fromClosedRange(inRangeStart.getOfType(), inRangeEnd.getOfType(), inStep.getOfType()).forEach {
                outIndex.set(it)
                outBody()
            }
            out()
        }
    }
}
