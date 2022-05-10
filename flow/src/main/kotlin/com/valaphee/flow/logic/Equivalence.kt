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

package com.valaphee.flow.logic

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.DataPath
import com.valaphee.flow.OperatorABNode
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.Node
import com.valaphee.flow.spec.Out
import kotlinx.coroutines.CoroutineScope

/**
 * @author Kevin Ludwig
 */
@Node("Logic/Equivalence")
class Equivalence(
    @get:In ("A")     @get:JsonProperty("in_a") override val inA: DataPath,
    @get:In ("B")     @get:JsonProperty("in_b") override val inB: DataPath,
    @get:Out("A = B") @get:JsonProperty("out" ) override val out: DataPath
) : OperatorABNode() {
    override fun run(scope: CoroutineScope) {
        out.set {
            val inA = inA.get()
            val inB = inB.get()
            inA != inB
        }
    }
}
