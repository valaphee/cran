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

package com.valaphee.cran.nesting

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.cran.node.Node
import com.valaphee.cran.node.Str
import com.valaphee.cran.spec.Const
import com.valaphee.cran.spec.In
import com.valaphee.cran.spec.NodeType

/**
 * @author Kevin Ludwig
 */
@NodeType("Nesting/Control Output")
class ControlOutput(
    type: String,
    @get:Const("Name", Str) @get:JsonProperty("name") val name: String,
    @get:Const("Json", Str) @get:JsonProperty("json") val json: String,
    @get:In   (""         ) @get:JsonProperty("in"  ) val `in`: Int   ,
) : Node(type)
