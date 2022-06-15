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

package com.valaphee.cran.node.logic

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.cran.node.BinaryOperation
import com.valaphee.cran.node.Bit
import com.valaphee.cran.spec.In
import com.valaphee.cran.spec.NodeDecl
import com.valaphee.cran.spec.Out

/**
 * @author Kevin Ludwig
 */
@NodeDecl("Logic/Or")
class Or(
    type: String,
    @get:In ("A"    , Bit) @get:JsonProperty("in_1") override val in1: Int,
    @get:In ("B"    , Bit) @get:JsonProperty("in_2") override val in2: Int,
    @get:Out("A ∨ B", Bit) @get:JsonProperty("out" ) override val out: Int
) : BinaryOperation(type) {
    init {
        out requires intArrayOf(in1, in2)
    }
}
