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
import com.valaphee.flow.StatelessNode
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.Node
import com.valaphee.flow.spec.Out
import com.valaphee.flow.spec.Type

/**
 * @author Kevin Ludwig
 */
@Node("Logic/And")
class And(
    @get:In ("A"    , Type.Bin) @get:JsonProperty("in_a") val inA: DataPath,
    @get:In ("B"    , Type.Bin) @get:JsonProperty("in_b") val inB: DataPath,
    @get:Out("A ∧ B", Type.Bin) @get:JsonProperty("out" ) val out: DataPath
) : StatelessNode() {
    override fun initialize() {
        out.set {
            val inA = inA.get()
            val inB = inB.get()
            if (inA is Boolean && inB is Boolean) inA and inB else throw DataPathException.invalidTypeInExpression("$inA ∧ $inB")
        }
    }
}
