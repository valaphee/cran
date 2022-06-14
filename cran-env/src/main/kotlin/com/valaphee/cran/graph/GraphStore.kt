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
import com.hazelcast.map.MapStore
import com.valaphee.cran.injector
import java.io.File
import java.util.Base64
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * @author Kevin Ludwig
 */
class GraphStore : MapStore<String, GraphEnv> {
    private val path = File("data").also { it.mkdir() }
    @Inject private lateinit var objectMapper: ObjectMapper

    init {
        injector.injectMembers(this)
    }

    override fun load(key: String) = File(path, key.graphToFileName()).takeIf { it.exists() }?.let { GZIPInputStream(it.inputStream()).use { objectMapper.readValue<GraphEnv>(it) } }

    override fun loadAll(keys: Collection<String>) = keys.mapNotNull { key -> load(key)?.let { key to it } }.toMap()

    override fun loadAllKeys() = path.list()?.mapNotNull { load(it)?.name } ?: emptyList()

    override fun deleteAll(keys: Collection<String>) {
        keys.forEach(::delete)
    }

    override fun delete(key: String) {
        File(path, key.graphToFileName()).delete()
    }

    override fun storeAll(map: Map<String, GraphEnv>) {
        map.forEach(::store)
    }

    override fun store(key: String, value: GraphEnv) {
        GZIPOutputStream(File(path, key.graphToFileName()).outputStream()).use { objectMapper.writeValue(it, value) }
    }

    companion object {
        private fun String.graphToFileName() = "${Base64.getUrlEncoder().encodeToString(lowercase().toByteArray())}.gph"
    }
}
