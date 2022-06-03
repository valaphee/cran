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

package com.valaphee.cran.graph

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Inject
import com.google.inject.Singleton
import com.valaphee.cran.GraphManager
import com.valaphee.cran.Scope
import com.valaphee.cran.spec.Spec
import com.valaphee.cran.svc.graph.v1.DeleteGraphRequest
import com.valaphee.cran.svc.graph.v1.DeleteGraphResponse
import com.valaphee.cran.svc.graph.v1.GetSpecRequest
import com.valaphee.cran.svc.graph.v1.GetSpecResponse
import com.valaphee.cran.svc.graph.v1.GraphServiceGrpc.GraphServiceImplBase
import com.valaphee.cran.svc.graph.v1.ListGraphRequest
import com.valaphee.cran.svc.graph.v1.ListGraphResponse
import com.valaphee.cran.svc.graph.v1.UpdateGraphRequest
import com.valaphee.cran.svc.graph.v1.UpdateGraphResponse
import io.github.classgraph.ClassGraph
import io.grpc.stub.StreamObserver
import org.apache.logging.log4j.LogManager
import java.util.UUID
import kotlin.concurrent.thread

/**
 * @author Kevin Ludwig
 */
@Singleton
class GraphServiceImpl @Inject constructor(
    private val objectMapper: ObjectMapper
) : GraphServiceImplBase(), GraphManager {
    private val spec: Spec
    private val graphs = mutableSetOf<GraphImpl>()
    private val scopes = mutableMapOf<UUID, Scope>()

    init {
        Runtime.getRuntime().addShutdownHook(thread(false) { scopes.forEach { (id, scope) -> graphs.find { it.id ==  id}?.shutdown(scope) } })

        ClassGraph().scan().use {
            spec = Spec(it.getResourcesMatchingWildcard("**.spec.json").urLs.flatMap { objectMapper.readValue<Spec>(it).nodes.onEach { log.info("Built-in node {} found", it.name) } })
            graphs += it.getResourcesMatchingWildcard("**.gph").urLs.map { objectMapper.readValue<GraphImpl>(it).also { log.info("Built-in node {} with graph found", it.name) } }
        }
    }

    override fun getGraph(name: String) = graphs.find { it.name == name }

    override fun getSpec(request: GetSpecRequest, responseObserver: StreamObserver<GetSpecResponse>) {
        responseObserver.onNext(GetSpecResponse.newBuilder().setSpec(objectMapper.writeValueAsString(Spec(spec.nodes + graphs.map { it.toSpec() }))).build())
        responseObserver.onCompleted()
    }

    override fun listGraph(request: ListGraphRequest, responseObserver: StreamObserver<ListGraphResponse>) {
        responseObserver.onNext(ListGraphResponse.newBuilder().apply { addAllGraph(graphs.map { objectMapper.writeValueAsString(it) }) }.build())
        responseObserver.onCompleted()
    }

    override fun updateGraph(request: UpdateGraphRequest, responseObserver: StreamObserver<UpdateGraphResponse>) {
        val graph = objectMapper.readValue<GraphImpl>(request.graph.toByteArray())
        graphs.find { it.id == graph.id }?.let {
            graphs -= it
            it.shutdown(checkNotNull(scopes[graph.id]))
        }
        graphs += graph
        graph.initialize(Scope(objectMapper, this).also { scopes[graph.id] = it })
        responseObserver.onNext(UpdateGraphResponse.getDefaultInstance())
        responseObserver.onCompleted()
    }

    override fun deleteGraph(request: DeleteGraphRequest, responseObserver: StreamObserver<DeleteGraphResponse>) {
        val graphId = UUID.fromString(request.graphId)
        graphs.find { it.id == graphId }?.let {
            graphs -= it
            it.shutdown(checkNotNull(scopes[graphId]))
        }
        responseObserver.onNext(DeleteGraphResponse.getDefaultInstance())
        responseObserver.onCompleted()
    }

    companion object {
        private val log = LogManager.getLogger(GraphServiceImpl::class.java)
    }
}
