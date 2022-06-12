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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.guice.GuiceAnnotationIntrospector
import com.fasterxml.jackson.module.guice.GuiceInjectableValues
import com.fasterxml.jackson.module.kotlin.addDeserializer
import com.fasterxml.jackson.module.kotlin.addSerializer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Provides
import com.google.inject.Singleton
import com.hazelcast.core.EntryEvent
import com.hazelcast.core.EntryListener
import com.hazelcast.core.Hazelcast
import com.hazelcast.map.MapEvent
import com.valaphee.cran.graph.GraphImpl
import com.valaphee.cran.graph.GraphLookup
import com.valaphee.cran.impl.Implementation
import com.valaphee.cran.node.math.vector.DoubleVectorDeserializer
import com.valaphee.cran.node.math.vector.DoubleVectorSerializer
import com.valaphee.cran.node.math.vector.IntVectorDeserializer
import com.valaphee.cran.node.math.vector.IntVectorSerializer
import com.valaphee.cran.spec.Spec
import io.github.classgraph.ClassGraph
import jdk.incubator.vector.DoubleVector
import jdk.incubator.vector.IntVector
import kotlinx.coroutines.asCoroutineDispatcher
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.io.IoBuilder
import java.util.concurrent.Executors

lateinit var injector: Injector

fun main() {
    System.setIn(null)
    System.setOut(IoBuilder.forLogger(LogManager.getRootLogger()).setLevel(Level.INFO).buildPrintStream())
    System.setErr(IoBuilder.forLogger(LogManager.getRootLogger()).setLevel(Level.ERROR).buildPrintStream())

    injector = Guice.createInjector(object : AbstractModule() {
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

    val log = LogManager.getLogger()

    val objectMapper = injector.getInstance(ObjectMapper::class.java)

    val hazelcast = Hazelcast.newHazelcastInstance()
    val nodeSpecs = hazelcast.getReplicatedMap<String, Spec.Node>("node_specs")
    val nodeImpls = mutableListOf<Implementation>()
    val graphs = hazelcast.getReplicatedMap<String, GraphImpl>("graphs")
    val graphLookup = object : GraphLookup {
        override fun getGraph(name: String) = graphs[name]
    }

    ClassGraph().scan().use {
        val spec = it.getResourcesMatchingWildcard("**.spec.json").urLs.map { objectMapper.readValue<Spec>(it).also { it.nodes.onEach { log.info("Built-in node '{}' found", it.name) } } }.reduce { acc, spec -> acc + spec }
        nodeSpecs.putAll(spec.nodes.onEach { log.info("Built-in node '{}' found", it.name) }.associateBy { it.name })
        spec.nodesImpls[""]?.let { nodeImpls.addAll(it.mapNotNull { Class.forName(it).kotlin.objectInstance as Implementation? }) }
        graphs.putAll(it.getResourcesMatchingWildcard("**.gph").urLs.map { objectMapper.readValue<GraphImpl>(it).also { log.info("Built-in graph '{}' found", it.name) } }.associateBy { it.name })
    }

    val coroutineDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    graphs.addEntryListener(object : EntryListener<String, GraphImpl> {
        override fun entryAdded(event: EntryEvent<String, GraphImpl>) {
            event.value.run(objectMapper, nodeImpls, graphLookup, coroutineDispatcher)
        }

        override fun entryUpdated(event: EntryEvent<String, GraphImpl>) {
            event.oldValue.shutdown()
            event.value.run(objectMapper, nodeImpls, graphLookup, coroutineDispatcher)
        }

        override fun entryRemoved(event: EntryEvent<String, GraphImpl>) {
            event.value.shutdown()
        }

        override fun entryEvicted(event: EntryEvent<String, GraphImpl>) {
            event.value.shutdown()
        }

        override fun entryExpired(event: EntryEvent<String, GraphImpl>) {
            event.value.shutdown()
        }

        override fun mapCleared(event: MapEvent) = Unit

        override fun mapEvicted(event: MapEvent) = Unit
    })
}
