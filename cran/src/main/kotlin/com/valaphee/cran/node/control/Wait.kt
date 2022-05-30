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
import com.valaphee.cran.Scope
import com.valaphee.cran.node.Num
import com.valaphee.cran.node.Task
import com.valaphee.cran.spec.In
import com.valaphee.cran.spec.NodeType
import com.valaphee.cran.spec.Out
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/**
 * @author Kevin Ludwig
 */
@NodeType("Control/Wait")
class Wait(
    type: String,
    @get:In ("Timeout" , Num) @get:JsonProperty("in_timeout"   )          val inTimeout  : Int,
    @get:In ("Begin"        ) @get:JsonProperty("in_begin"     ) override val inBegin    : Int,
    @get:In ("Abort"        ) @get:JsonProperty("in_abort"     ) override val inAbort    : Int,
    @get:Out("OnFinish"     ) @get:JsonProperty("out_on_finish")          val outOnFinish: Int,
    @get:Out("Subgraph"     ) @get:JsonProperty("out_subgraph" ) override val outSubgraph: Int,
) : Task(type) {
    override suspend fun onBegin(scope: Scope) {
        val inTimeout = scope.dataPath(inTimeout).getOfType<Int>()

        delay(inTimeout.seconds)
        scope.controlPath(outOnFinish)()
    }
}
