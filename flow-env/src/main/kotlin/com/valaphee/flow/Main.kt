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

package com.valaphee.flow

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
import com.valaphee.flow.graph.GraphServiceImpl
import com.valaphee.flow.node.math.vector2.Double2Deserializer
import com.valaphee.flow.node.math.vector2.Double2Serializer
import com.valaphee.flow.node.math.vector2.Int2Deserializer
import com.valaphee.flow.node.math.vector2.Int2Serializer
import com.valaphee.flow.node.math.vector3.Double3Deserializer
import com.valaphee.flow.node.math.vector3.Double3Serializer
import com.valaphee.flow.node.math.vector3.Int3Deserializer
import com.valaphee.flow.node.math.vector3.Int3Serializer
import com.valaphee.flow.node.math.vector4.Double4Deserializer
import com.valaphee.flow.node.math.vector4.Double4Serializer
import com.valaphee.flow.node.math.vector4.Int4Deserializer
import com.valaphee.flow.node.math.vector4.Int4Serializer
import com.valaphee.foundry.math.Double2
import com.valaphee.foundry.math.Double3
import com.valaphee.foundry.math.Double4
import com.valaphee.foundry.math.Int2
import com.valaphee.foundry.math.Int3
import com.valaphee.foundry.math.Int4
import com.valaphee.svc.graph.v1.GraphServiceGrpc.GraphServiceImplBase
import io.grpc.ServerBuilder
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.io.IoBuilder

fun main(arguments: Array<String>) {
    val argumentParser = ArgParser("flow")
    val port by argumentParser.option(ArgType.Int, "port", "p", "Port").default(8080)
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
                .addSerializer(Int2::class   , Int2Serializer   ).addDeserializer(Int2::class   , Int2Deserializer   )
                .addSerializer(Double2::class, Double2Serializer).addDeserializer(Double2::class, Double2Deserializer)
                .addSerializer(Int3::class   , Int3Serializer   ).addDeserializer(Int3::class   , Int3Deserializer   )
                .addSerializer(Double3::class, Double3Serializer).addDeserializer(Double3::class, Double3Deserializer)
                .addSerializer(Int4::class   , Int4Serializer   ).addDeserializer(Int4::class   , Int4Deserializer   )
                .addSerializer(Double4::class, Double4Serializer).addDeserializer(Double4::class, Double4Deserializer)
        ).apply {
            val guiceAnnotationIntrospector = GuiceAnnotationIntrospector()
            setAnnotationIntrospectors(AnnotationIntrospectorPair(guiceAnnotationIntrospector, serializationConfig.annotationIntrospector), AnnotationIntrospectorPair(guiceAnnotationIntrospector, deserializationConfig.annotationIntrospector))
            injectableValues = GuiceInjectableValues(injector)
        }
    })

    val server = ServerBuilder
        .forPort(port)
        .addService(injector.getInstance(GraphServiceImplBase::class.java))
        .build()
        .start()
    LogManager.getLogger("Main").info("Listening on $port")
    server.awaitTermination()
}
