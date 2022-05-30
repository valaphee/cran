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

import com.google.common.util.concurrent.ThreadFactoryBuilder
import io.netty.channel.ChannelFactory
import io.netty.channel.EventLoopGroup
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollDatagramChannel
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.kqueue.KQueue
import io.netty.channel.kqueue.KQueueDatagramChannel
import io.netty.channel.kqueue.KQueueEventLoopGroup
import io.netty.channel.kqueue.KQueueServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.DatagramChannel
import io.netty.channel.socket.ServerSocketChannel
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import java.util.concurrent.ThreadFactory

val CurrentUnderlyingNetworking = if (Epoll.isAvailable()) UnderlyingNetworking.Epoll else if (KQueue.isAvailable()) UnderlyingNetworking.Kqueue else UnderlyingNetworking.Nio
val BossGroup = CurrentUnderlyingNetworking.groupFactory(0, ThreadFactoryBuilder().build())
val WorkerGroup = CurrentUnderlyingNetworking.groupFactory(0, ThreadFactoryBuilder().build())

/**
 * @author Kevin Ludwig
 */
enum class UnderlyingNetworking(
    val groupFactory: (Int, ThreadFactory) -> EventLoopGroup,
    val serverSocketChannel: ChannelFactory<ServerSocketChannel>,
    val datagramChannel: ChannelFactory<DatagramChannel>
) {
    Epoll({ threadCount, threadFactory -> EpollEventLoopGroup(threadCount, threadFactory) }, { EpollServerSocketChannel() }, { EpollDatagramChannel() }),
    Kqueue({ threadCount, threadFactory -> KQueueEventLoopGroup(threadCount, threadFactory) }, { KQueueServerSocketChannel() }, { KQueueDatagramChannel() }),
    Nio({ threadCount, threadFactory -> NioEventLoopGroup(threadCount, threadFactory) }, { NioServerSocketChannel() }, { NioDatagramChannel() })
}
