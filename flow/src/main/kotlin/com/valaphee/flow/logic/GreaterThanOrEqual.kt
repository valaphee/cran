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
import com.valaphee.flow.DataPathException
import com.valaphee.flow.LazyNode
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.Node
import com.valaphee.flow.spec.Out
import kotlinx.coroutines.CoroutineScope

/**
 * @author Kevin Ludwig
 */
@Node("Logic/Greater Than or Equal")
class GreaterThanOrEqual(
    @get:In ("A")     @get:JsonProperty("in_a") val inA: DataPath,
    @get:In ("B")     @get:JsonProperty("in_b") val inB: DataPath,
    @get:Out("A ≥ B") @get:JsonProperty("out" ) val out: DataPath
) : LazyNode() {
    override fun initialize(scope: CoroutineScope) {
        out.set {
            val inA = inA.get()
            val inB = inB.get()
            val result = Compare.compare(inA, inB)
            if (result != Int.MAX_VALUE) result >= 0 else DataPathException.invalidTypeInExpression("$inA ≥ $inB")
        }
    }
}
