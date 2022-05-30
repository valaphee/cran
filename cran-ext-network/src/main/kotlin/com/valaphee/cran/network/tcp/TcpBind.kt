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

package com.valaphee.cran.network.tcp

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.cran.Scope
import com.valaphee.cran.network.BossGroup
import com.valaphee.cran.network.CurrentUnderlyingNetworking
import com.valaphee.cran.network.WorkerGroup
import com.valaphee.cran.node.Int
import com.valaphee.cran.node.Str
import com.valaphee.cran.node.Task
import com.valaphee.cran.node.Und
import com.valaphee.cran.spec.In
import com.valaphee.cran.spec.NodeType
import com.valaphee.cran.spec.Out
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import kotlinx.coroutines.runBlocking

/**
 * @author Kevin Ludwig
 */
@NodeType("Network/Tcp/Bind")
class TcpBind(
    type: String,
    @get:In ("Begin"        ) @get:JsonProperty("in_begin"     ) override val inBegin    : Int,
    @get:In ("Abort"        ) @get:JsonProperty("in_abort"     ) override val inAbort    : Int,
    @get:In ("Host"    , Str) @get:JsonProperty("in_host"      )          val inHost     : Int,
    @get:In ("Port"    , Int) @get:JsonProperty("in_port"      )          val inPort     : Int,
    @get:Out("Subgraph"     ) @get:JsonProperty("out_subgraph" ) override val outSubgraph: Int,
    @get:Out("OnAccept"     ) @get:JsonProperty("out_on_accept")          val outOnAccept: Int,
    @get:Out("Accept"  , Und) @get:JsonProperty("out_accept"   )          val outAccept  : Int
) : Task(type) {
    override suspend fun onBegin(scope: Scope) {
        val inHost = scope.dataPath(inHost).getOfType<String>()
        val inPort = scope.dataPath(inPort).getOfType<Int>()
        val outOnAccept = scope.controlPath(outOnAccept)
        val outAccept = scope.dataPath(outAccept)

        @Suppress("UNCHECKED_CAST")
        ServerBootstrap()
            .group(BossGroup, WorkerGroup)
            .channelFactory(CurrentUnderlyingNetworking.serverSocketChannel)
            .childHandler(object : ChannelInitializer<Channel>() {
                override fun initChannel(channel: Channel) {
                    outAccept.set(channel)
                    runBlocking { outOnAccept() }
                }
            })
            .localAddress(inHost, inPort)
            .bind().sync()
            .channel().closeFuture().sync()
    }
}
