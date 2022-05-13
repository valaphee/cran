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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.smile.databind.SmileMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.valaphee.flow.manifest.Manifest
import com.valaphee.flow.spec.Spec
import com.valaphee.svc.graph.v1.GetSpecRequest
import com.valaphee.svc.graph.v1.GraphServiceGrpc
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

val ObjectMapper: ObjectMapper = jacksonObjectMapper()
val SmileObjectMapper: ObjectMapper = SmileMapper().registerKotlinModule()

val ServiceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
val Channel: ManagedChannel = ManagedChannelBuilder.forAddress("localhost", 8080).usePlaintext().build()
val GraphService: GraphServiceGrpc.GraphServiceBlockingStub = GraphServiceGrpc.newBlockingStub(Channel)

val Spec = SmileObjectMapper.readValue<Spec>(GraphService.getSpec(GetSpecRequest.getDefaultInstance()).spec.toByteArray())
val Manifest = ObjectMapper.readValue<Manifest>(Main::class.java.getResource("/manifest.json")!!)
