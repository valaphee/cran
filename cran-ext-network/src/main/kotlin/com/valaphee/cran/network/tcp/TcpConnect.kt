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
import com.valaphee.cran.network.CurrentUnderlyingNetworking
import com.valaphee.cran.network.WorkerGroup
import com.valaphee.cran.node.Int
import com.valaphee.cran.node.State
import com.valaphee.cran.node.Str
import com.valaphee.cran.node.Und
import com.valaphee.cran.spec.In
import com.valaphee.cran.spec.NodeType
import com.valaphee.cran.spec.Out
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import kotlinx.coroutines.runBlocking

/**
 * @author Kevin Ludwig
 */
@NodeType("Network/Tcp/Connect")
class TcpConnect(
    type: String,
    @get:In ("Begin"           ) @get:JsonProperty("in_begin"          ) override val inBegin     : Int,
    @get:In ("Abort"           ) @get:JsonProperty("in_abort"          ) override val inAbort     : Int,
    @get:In ("Local Host" , Str) @get:JsonProperty("in_local_host"     )          val inLocalHost : Int,
    @get:In ("Local Port" , Int) @get:JsonProperty("in_local_port"     )          val inLocalPort : Int,
    @get:In ("Remote Host", Str) @get:JsonProperty("in_remote_host"    )          val inRemoteHost: Int,
    @get:In ("Remote Port", Int) @get:JsonProperty("in_remote_port"    )          val inRemotePort: Int,
    @get:Out("Subgraph"        ) @get:JsonProperty("out_subgraph"      ) override val outSubgraph : Int,
    @get:Out("On Connect"      ) @get:JsonProperty("out_on_connect")              val outOnConnect: Int,
    @get:Out(""           , Und) @get:JsonProperty("out"               )          val out         : Int
) : State(type) {
    override suspend fun onBegin(scope: Scope) {
        val inLocalHost = scope.dataPath(inLocalHost).getOfType<String>()
        val inLocalPort = scope.dataPath(inLocalPort).getOfType<Int>()
        val inRemoteHost = scope.dataPath(inRemoteHost).getOfType<String>()
        val inRemotePort = scope.dataPath(inRemotePort).getOfType<Int>()
        val outOnConnect = scope.controlPath(outOnConnect)
        val out = scope.dataPath(out)

        @Suppress("UNCHECKED_CAST")
        Bootstrap()
            .group(WorkerGroup)
            .channelFactory(CurrentUnderlyingNetworking.serverSocketChannel)
            .handler(object : ChannelInitializer<Channel>() {
                override fun initChannel(channel: Channel) {
                    out.set(channel)
                    runBlocking { outOnConnect() }
                }
            })
            .localAddress(inLocalHost, inLocalPort)
            .remoteAddress(inRemoteHost, inRemotePort)
            .bind().sync()
            .channel().closeFuture().sync()
    }
}
