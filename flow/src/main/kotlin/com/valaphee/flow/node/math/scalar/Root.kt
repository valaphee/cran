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

package com.valaphee.flow.node.math.scalar

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.node.Node
import com.valaphee.flow.node.Num
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.NodeType
import com.valaphee.flow.spec.Out

/**
 * @author Kevin Ludwig
 */
@NodeType("Math/Scalar/Root")
class Root(
    type: String,
    @get:In ("x"  , Num) @get:JsonProperty("in_x") val inX: Int,
    @get:In ("n"  , Num) @get:JsonProperty("in_n") val inN: Int,
    @get:Out("ⁿ√x", Num) @get:JsonProperty("out" ) val out: Int
) : Node(type)
