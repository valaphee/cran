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

package com.valaphee.cran

import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.guice.GuiceAnnotationIntrospector
import com.fasterxml.jackson.module.guice.GuiceInjectableValues
import com.fasterxml.jackson.module.kotlin.addDeserializer
import com.fasterxml.jackson.module.kotlin.addSerializer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Provides
import com.google.inject.Singleton
import com.valaphee.cran.graph.GraphServiceImpl
import com.valaphee.cran.node.math.vector.DoubleVectorDeserializer
import com.valaphee.cran.node.math.vector.DoubleVectorSerializer
import com.valaphee.cran.node.math.vector.IntVectorDeserializer
import com.valaphee.cran.node.math.vector.IntVectorSerializer
import com.valaphee.cran.security.TlsSubcommand
import com.valaphee.cran.svc.graph.v1.GraphServiceGrpc.GraphServiceImplBase
import io.grpc.netty.GrpcSslContexts
import io.grpc.netty.NettyServerBuilder
import io.netty.handler.ssl.ClientAuth
import io.netty.handler.ssl.SslContextBuilder
import jdk.incubator.vector.DoubleVector
import jdk.incubator.vector.IntVector
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.io.IoBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.File
import java.net.InetSocketAddress
import java.security.Security

fun main(arguments: Array<String>) {
    Security.addProvider(BouncyCastleProvider())

    val argumentParser = ArgParser("cran-env")
    val host by argumentParser.option(ArgType.String, "host", "H", "Host").default("localhost")
    val port by argumentParser.option(ArgType.Int, "port", "p", "Port").default(8080)
    val serverCer by argumentParser.option(ArgType.String, "server-cer", description = "Server Certificate Chain").default("tls/server_cer.pem")
    val serverKey by argumentParser.option(ArgType.String, "server-key", description = "Server Private Key").default("tls/server_key.pem")
    val clientCer by argumentParser.option(ArgType.String, "client-cer", description = "Client Certificate").default("tls/client_cer.pem")
    argumentParser.subcommands(TlsSubcommand)
    argumentParser.parse(arguments)

    System.setIn(null)
    System.setOut(IoBuilder.forLogger(LogManager.getRootLogger()).setLevel(Level.INFO).buildPrintStream())
    System.setErr(IoBuilder.forLogger(LogManager.getRootLogger()).setLevel(Level.ERROR).buildPrintStream())

    val injector = Guice.createInjector(object : AbstractModule() {
        override fun configure() {
            bind(GraphServiceImplBase::class.java).to(GraphServiceImpl::class.java)
        }

        @Provides
        @Singleton
        fun objectMapper(injector: Injector) = jacksonObjectMapper().registerModule(
            SimpleModule()
                .addSerializer(IntVector::class   , IntVectorSerializer   ).addDeserializer(IntVector::class   , IntVectorDeserializer   )
                .addSerializer(DoubleVector::class, DoubleVectorSerializer).addDeserializer(DoubleVector::class, DoubleVectorDeserializer)
        ).apply {
            val guiceAnnotationIntrospector = GuiceAnnotationIntrospector()
            setAnnotationIntrospectors(AnnotationIntrospectorPair(guiceAnnotationIntrospector, serializationConfig.annotationIntrospector), AnnotationIntrospectorPair(guiceAnnotationIntrospector, deserializationConfig.annotationIntrospector))
            injectableValues = GuiceInjectableValues(injector)
        }
    })

    val server = NettyServerBuilder
        .forAddress(InetSocketAddress(host, port))
        .addService(injector.getInstance(GraphServiceImplBase::class.java))
        .sslContext(GrpcSslContexts.configure(SslContextBuilder.forServer(File(serverCer), File(serverKey)).trustManager(File(clientCer)).clientAuth(ClientAuth.REQUIRE)).build())
        .build()
        .start()
    LogManager.getLogger("Main").info("Listening on $port")
    server.awaitTermination()
}
