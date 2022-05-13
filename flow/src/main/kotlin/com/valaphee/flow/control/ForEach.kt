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

package com.valaphee.flow.control

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.ControlPath
import com.valaphee.flow.DataPath
import com.valaphee.flow.StatefulNode
import com.valaphee.flow.getOrThrow
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.Node
import com.valaphee.flow.spec.Out
import com.valaphee.flow.spec.Type

/**
 * @author Kevin Ludwig
 */
@Node("Control/For Each")
class ForEach(
    @get:In                   @get:JsonProperty("in"       ) override val `in`    : ControlPath,
    @get:In (type = Type.Arr) @get:JsonProperty("in_value" )          val inValue : DataPath   ,
    @get:Out("Body"         ) @get:JsonProperty("out_body" )          val outBody : ControlPath,
    @get:Out("Exit"         ) @get:JsonProperty("out"      )          val out     : ControlPath,
    @get:Out("Value"        ) @get:JsonProperty("out_value")          val outValue: DataPath   ,
) : StatefulNode() {
    override fun initialize() {
        `in`.declare {
            inValue.getOrThrow<Iterable<*>>("in_value").forEach {
                outValue.set(it)
                outBody()
            }
            out()
        }
    }
}
