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

package com.valaphee.cran.settings

import com.fasterxml.jackson.annotation.JsonIgnore
import io.grpc.ManagedChannel
import io.grpc.netty.GrpcSslContexts
import io.grpc.netty.NegotiationType
import io.grpc.netty.NettyChannelBuilder
import io.netty.handler.ssl.SslContextBuilder
import tornadofx.getValue
import tornadofx.setValue
import tornadofx.toProperty
import java.io.File
import java.security.KeyStore
import java.security.cert.X509Certificate
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * @author Kevin Ludwig
 */
class Settings(
    gridX: Int = 5,
    gridY: Int = 5
) {
    @JsonIgnore private val gridXProperty = gridX.toProperty()
    @JsonIgnore private val gridYProperty = gridY.toProperty()

    var gridX by gridXProperty
    var gridY by gridYProperty

    class Environment(
        target: String,
        clientCer: String,
        clientKey: String,
        serverCer: String
    ) {
        @JsonIgnore internal val targetProperty = target.toProperty()
        @JsonIgnore internal val clientCerProperty = clientCer.toProperty()
        @JsonIgnore internal val clientKeyProperty = clientKey.toProperty()
        @JsonIgnore internal val serverCerProperty = serverCer.toProperty()

        var target: String by targetProperty
        var clientCer: String by clientCerProperty
        var clientKey: String by clientKeyProperty
        var serverCer: String by serverCerProperty

        fun toChannel(): ManagedChannel = NettyChannelBuilder.forTarget(target).negotiationType(NegotiationType.TLS).sslContext(GrpcSslContexts.configure(SslContextBuilder.forClient().keyManager(File(clientCer), File(clientKey)).trustManager(object : X509TrustManager {
            private val parent = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).apply { init(null as KeyStore?) }.trustManagers.find { it is X509TrustManager } as X509TrustManager

            override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String) = Unit

            override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String) = Unit

            override fun getAcceptedIssuers() = parent.acceptedIssuers
        })).build()).build()
    }
}
