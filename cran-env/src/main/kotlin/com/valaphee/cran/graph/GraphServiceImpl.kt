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
import com.valaphee.cran.node.Entry
import com.valaphee.cran.spec.Spec
import com.valaphee.cran.svc.graph.v1.DeleteGraphRequest
import com.valaphee.cran.svc.graph.v1.DeleteGraphResponse
import com.valaphee.cran.svc.graph.v1.GetSpecRequest
import com.valaphee.cran.svc.graph.v1.GetSpecResponse
import com.valaphee.cran.svc.graph.v1.GraphServiceGrpc.GraphServiceImplBase
import com.valaphee.cran.svc.graph.v1.ListGraphRequest
import com.valaphee.cran.svc.graph.v1.ListGraphResponse
import com.valaphee.cran.svc.graph.v1.RunGraphRequest
import com.valaphee.cran.svc.graph.v1.RunGraphResponse
import com.valaphee.cran.svc.graph.v1.StopGraphRequest
import com.valaphee.cran.svc.graph.v1.StopGraphResponse
import com.valaphee.cran.svc.graph.v1.UpdateGraphRequest
import com.valaphee.cran.svc.graph.v1.UpdateGraphResponse
import io.github.classgraph.ClassGraph
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import org.apache.logging.log4j.LogManager
import java.util.UUID
import java.util.concurrent.Executors

/**
 * @author Kevin Ludwig
 */
@Singleton
class GraphServiceImpl @Inject constructor(
    private val objectMapper: ObjectMapper
) : GraphServiceImplBase(), GraphManager, CoroutineScope {
    private val executor = Executors.newSingleThreadExecutor()
    override val coroutineContext get() = executor.asCoroutineDispatcher()

    private val spec: Spec
    private val graphs = mutableMapOf<String, GraphImpl>()
    private val scopes = mutableMapOf<UUID, Scope>()

    init {
        ClassGraph().scan().use {
            spec = Spec(it.getResourcesMatchingWildcard("**.spec.json").urLs.flatMap { objectMapper.readValue<Spec>(it).nodes.onEach { log.info("Built-in node '{}' found", it.name) } })
            graphs += it.getResourcesMatchingWildcard("**.gph").urLs.map { objectMapper.readValue<GraphImpl>(it).also { log.info("Built-in node '{}' with graph found", it.name) } }.associateBy { it.name }
        }
    }

    override fun getGraph(name: String) = graphs[name]

    override fun getSpec(request: GetSpecRequest, responseObserver: StreamObserver<GetSpecResponse>) {
        responseObserver.onNext(GetSpecResponse.newBuilder().setSpec(objectMapper.writeValueAsString(spec)).build())
        responseObserver.onCompleted()
    }

    override fun listGraph(request: ListGraphRequest, responseObserver: StreamObserver<ListGraphResponse>) {
        responseObserver.onNext(ListGraphResponse.newBuilder().apply { addAllGraphs(graphs.values.map { objectMapper.writeValueAsString(it) }) }.build())
        responseObserver.onCompleted()
    }

    override fun updateGraph(request: UpdateGraphRequest, responseObserver: StreamObserver<UpdateGraphResponse>) {
        val graph = objectMapper.readValue<GraphImpl>(request.graph.toByteArray())
        graphs[graph.name] = graph
        responseObserver.onNext(UpdateGraphResponse.getDefaultInstance())
        responseObserver.onCompleted()
    }

    override fun deleteGraph(request: DeleteGraphRequest, responseObserver: StreamObserver<DeleteGraphResponse>) {
        graphs.remove(request.graphName)
        responseObserver.onNext(DeleteGraphResponse.getDefaultInstance())
        responseObserver.onCompleted()
    }

    override fun runGraph(request: RunGraphRequest, responseObserver: StreamObserver<RunGraphResponse>) {
        val scopeId = UUID.randomUUID()
        graphs[request.graphName]?.let {
            val scope = Scope(objectMapper, this, it).also {
                scopes[scopeId] = it
                it.initialize()
            }
            it.nodes.forEach { if (it is Entry) launch { it(scope) } }
        }
        responseObserver.onNext(RunGraphResponse.newBuilder().setScopeId(scopeId.toString()).build())
        responseObserver.onCompleted()
    }

    override fun stopGraph(request: StopGraphRequest, responseObserver: StreamObserver<StopGraphResponse>) {
        scopes.remove(UUID.fromString(request.scopeId))?.shutdown()
        responseObserver.onNext(StopGraphResponse.getDefaultInstance())
        responseObserver.onCompleted()
    }

    companion object {
        private val log = LogManager.getLogger(GraphServiceImpl::class.java)
    }
}
