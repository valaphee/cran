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

import com.fasterxml.jackson.module.kotlin.readValue
import com.valaphee.flow.spec.Spec
import io.github.classgraph.ClassGraph
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.JacksonConverter
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.io.IoBuilder
import java.util.UUID

fun main(arguments: Array<String>) {
    val argumentParser = ArgParser("flow-api")
    val host by argumentParser.option(ArgType.String, "host", "H", "Host").default("localhost")
    val port by argumentParser.option(ArgType.Int, "port", "p", "Port").default(8080)
    argumentParser.parse(arguments)

    System.setIn(null)
    System.setOut(IoBuilder.forLogger(LogManager.getRootLogger()).setLevel(Level.INFO).buildPrintStream())
    System.setErr(IoBuilder.forLogger(LogManager.getRootLogger()).setLevel(Level.ERROR).buildPrintStream())

    embeddedServer(Netty, port, host, emptyList()) {
        install(Compression)
        install(ContentNegotiation) { register(ContentType.Application.Json, JacksonConverter(ObjectMapper)) }

        routing {
            val spec = ClassGraph().scan().use { Spec(it.getResourcesWithPath("spec.json").urLs.flatMap { ObjectMapper.readValue<Spec>(it).nodes }) }
            get("/v1/spec") { call.respond(spec) }

            val graphs = mutableMapOf<UUID, GraphImpl>()
            post("/v1/graph") {
                val graph = call.receive<GraphImpl>()
                if (graphs.containsKey(graph.id)) call.respond(HttpStatusCode.BadRequest) else {
                    call.respond(HttpStatusCode.OK)
                    graphs[graph.id] = graph
                    graph.initialize()
                }
            }
            delete("/v1/graph/{id}") {
                val id = UUID.fromString(call.parameters["id"])
                if (graphs.containsKey(id)) {
                    call.respond(HttpStatusCode.OK)
                    graphs.remove(id)!!.shutdown()
                } else call.respond(HttpStatusCode.NotFound)
            }
            get("/v1/graph/") { call.respond(graphs.values) }
        }
    }.start(true)
}
