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

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.valaphee.flow.manifest.Manifest
import com.valaphee.flow.spec.Spec
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.serialization.jackson.JacksonConverter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking

val ApiScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
val ObjectMapper: ObjectMapper = jacksonObjectMapper().setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
val HttpClient = HttpClient(OkHttp) {
    expectSuccess = false
    install(ContentNegotiation) { register(ContentType.Application.Json, JacksonConverter(ObjectMapper)) }
}
val Spec = runBlocking { HttpClient.get("http://localhost:8080/v1/spec").body<Spec>() }
val Manifest = ObjectMapper.readValue<Manifest>(Main::class.java.getResource("/manifest.json")!!)
