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

package com.valaphee.flow.loop

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.ControlPath
import com.valaphee.flow.DataPath
import com.valaphee.flow.EagerNode
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.Node
import com.valaphee.flow.spec.Out
import kotlinx.coroutines.CoroutineScope

/**
 * @author Kevin Ludwig
 */
@Node("For")
class For(
    @get:In (""     ) @get:JsonProperty("in"            ) override val `in`        : ControlPath,
    @get:In ("Start") @get:JsonProperty("in_range_start")          val inRangeStart: DataPath,
    @get:In ("End"  ) @get:JsonProperty("in_range_end"  )          val inRangeEnd  : DataPath,
    @get:In ("Step" ) @get:JsonProperty("in_step"       )          val inStep      : DataPath,
    @get:Out("Body" ) @get:JsonProperty("out_body"      )          val outBody     : ControlPath,
    @get:Out("Exit" ) @get:JsonProperty("out"           )          val out         : ControlPath,
) : EagerNode() {
    override fun run(scope: CoroutineScope) {
        `in`.collect(scope) {
            IntProgression.fromClosedRange((inRangeStart.get() as Number).toInt(), (inRangeEnd.get() as Number).toInt(), (inStep.get() as Number).toInt()).forEach { _ -> outBody.emit() }
            out.emit()
        }
    }
}
