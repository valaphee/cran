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

package com.valaphee.flow.network

import com.valaphee.flow.Scope
import com.valaphee.flow.node.Node
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.NodeType
import com.valaphee.flow.spec.Out
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import kotlinx.coroutines.runBlocking

/**
 * @author Kevin Ludwig
 */
@NodeType("Network/On Read")
class OnRead(
    type: String,
    @get:In (""       ) val `in`      : Int,
    @get:In ("Channel") val inChannel : Int,
    @get:Out(""       ) val out       : Int,
    @get:Out("Message") val outMessage: Int
) : Node(type) {
    override fun initialize(scope: Scope) {
        val `in` = scope.controlPath(`in`)
        val inChannel = scope.dataPath(inChannel)
        val out = scope.controlPath(out)
        val outMessage = scope.dataPath(outMessage)

        `in`.declare {
            inChannel.getOfType<Channel>().pipeline().addLast(object : ChannelInboundHandlerAdapter() {
                override fun channelRead(ctx: ChannelHandlerContext, message: Any) {
                    outMessage.set(message)
                    runBlocking { out() }
                }
            })
            out()
        }
    }
}
