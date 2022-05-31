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

package com.valaphee.cran.network

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.cran.Scope
import com.valaphee.cran.node.Node
import com.valaphee.cran.node.Und
import com.valaphee.cran.spec.In
import com.valaphee.cran.spec.NodeType
import com.valaphee.cran.spec.Out
import io.netty.channel.Channel

/**
 * @author Kevin Ludwig
 */
@NodeType("Network/Write")
class Write(
    type: String,
    @get:In (""            ) @get:JsonProperty("in"        ) val `in`     : Int,
    @get:In ("Channel", Und) @get:JsonProperty("in_channel") val inChannel: Int,
    @get:In ("Message", Und) @get:JsonProperty("in_message") val inMessage: Int,
    @get:Out(""            ) @get:JsonProperty("out"       ) val out      : Int,
) : Node(type) {
    override fun initialize(scope: Scope) {
        val `in` = scope.controlPath(`in`)
        val inChannel = scope.dataPath(inChannel)
        val inMessage = scope.dataPath(inMessage)
        val out = scope.controlPath(out)

        `in`.declare {
            inChannel.getOfType<Channel>().write(inMessage.get())
            out()
        }
    }
}
