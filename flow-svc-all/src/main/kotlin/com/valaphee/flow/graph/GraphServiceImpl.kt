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

package com.valaphee.flow.graph

import com.fasterxml.jackson.dataformat.smile.databind.SmileMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.protobuf.ByteString
import com.valaphee.flow.spec.Spec
import com.valaphee.svc.graph.v1.DeleteGraphRequest
import com.valaphee.svc.graph.v1.DeleteGraphResponse
import com.valaphee.svc.graph.v1.GetSpecRequest
import com.valaphee.svc.graph.v1.GetSpecResponse
import com.valaphee.svc.graph.v1.GraphServiceGrpc.GraphServiceImplBase
import com.valaphee.svc.graph.v1.ListGraphRequest
import com.valaphee.svc.graph.v1.ListGraphResponse
import com.valaphee.svc.graph.v1.UpdateGraphRequest
import com.valaphee.svc.graph.v1.UpdateGraphResponse
import io.github.classgraph.ClassGraph
import io.grpc.stub.StreamObserver
import java.util.UUID

/**
 * @author Kevin Ludwig
 */
class GraphServiceImpl : GraphServiceImplBase() {
    private val spec = GetSpecResponse.newBuilder().setSpec(ByteString.copyFrom(objectMapper.writeValueAsBytes(ClassGraph().scan().use { Spec(it.getResourcesMatchingWildcard("spec.*.json").urLs.flatMap { objectMapper.readValue<Spec>(it).nodes }) }))).build()
    private val graphs = mutableMapOf<UUID, GraphImpl>()

    override fun getSpec(request: GetSpecRequest, responseObserver: StreamObserver<GetSpecResponse>) {
        responseObserver.onNext(spec)
        responseObserver.onCompleted()
    }

    override fun listGraph(request: ListGraphRequest, responseObserver: StreamObserver<ListGraphResponse>) {
        responseObserver.onNext(ListGraphResponse.newBuilder().setGraphs(ByteString.copyFrom(objectMapper.writeValueAsBytes(graphs.values))).build())
        responseObserver.onCompleted()
    }

    override fun updateGraph(request: UpdateGraphRequest, responseObserver: StreamObserver<UpdateGraphResponse>) {
        val graph = objectMapper.readValue<GraphImpl>(request.graph.toByteArray())
        if (graphs.containsKey(graph.id)) graphs.remove(graph.id)!!.shutdown()
        graphs[graph.id] = graph
        graph.initialize()
        responseObserver.onNext(UpdateGraphResponse.getDefaultInstance())
        responseObserver.onCompleted()
    }

    override fun deleteGraph(request: DeleteGraphRequest, responseObserver: StreamObserver<DeleteGraphResponse>) {
        val graphId = UUID.fromString(request.graphId)
        if (graphs.containsKey(graphId)) graphs.remove(graphId)!!.shutdown()
        responseObserver.onNext(DeleteGraphResponse.getDefaultInstance())
        responseObserver.onCompleted()
    }

    companion object {
        private val objectMapper = SmileMapper().registerKotlinModule()
    }
}
