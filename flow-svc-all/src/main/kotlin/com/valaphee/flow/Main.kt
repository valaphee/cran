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
import com.fasterxml.jackson.module.guice.GuiceAnnotationIntrospector
import com.fasterxml.jackson.module.guice.GuiceInjectableValues
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Provides
import com.google.inject.Singleton
import com.valaphee.flow.graph.GraphServiceImpl
import com.valaphee.svc.graph.v1.GraphServiceGrpc.GraphServiceImplBase
import io.grpc.ServerBuilder
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.io.IoBuilder

fun main(arguments: Array<String>) {
    val argumentParser = ArgParser("flow-api")
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
        fun objectMapper(injector: Injector) = jacksonObjectMapper().apply {
            val guiceAnnotationIntrospector = GuiceAnnotationIntrospector()
            setAnnotationIntrospectors(AnnotationIntrospectorPair(guiceAnnotationIntrospector, serializationConfig.annotationIntrospector), AnnotationIntrospectorPair(guiceAnnotationIntrospector, deserializationConfig.annotationIntrospector))
            injectableValues = GuiceInjectableValues(injector)
        }
    })

    ServerBuilder
        .forPort(port)
        .addService(injector.getInstance(GraphServiceImplBase::class.java))
        .build()
        .start()
        .awaitTermination()
}
