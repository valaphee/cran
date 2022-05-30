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
import com.valaphee.flow.node.Int
import com.valaphee.flow.node.State
import com.valaphee.flow.spec.Const
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.NodeType
import com.valaphee.flow.spec.Out
import kotlinx.coroutines.delay

/**
 * @author Kevin Ludwig
 */
@NodeType("Control/Wait")
class Wait(
    type: String,
    @get:Const("Timeout" , Int) @get:JsonProperty("timeout"      )          val timeout    : Int,
    @get:In   ("Begin"        ) @get:JsonProperty("in_begin"     ) override val inBegin    : Int,
    @get:In   ("Abort"        ) @get:JsonProperty("in_abort"     ) override val inAbort    : Int,
    @get:Out  ("OnFinish"     ) @get:JsonProperty("out_on_finish")          val outOnFinish: Int,
    @get:Out  ("OnAbort"      ) @get:JsonProperty("out_on_abort" )          val outOnAbort : Int,
    @get:Out  ("Subgraph"     ) @get:JsonProperty("out_subgraph" ) override val outSubgraph: Int,
) : State(type) {
    override suspend fun onBegin(scope: Scope) {
        delay(timeout.toLong())
        scope.controlPath(outOnFinish)()
    }

    override suspend fun onAbort(scope: Scope) {
        scope.controlPath(outOnAbort)()
    }
}
