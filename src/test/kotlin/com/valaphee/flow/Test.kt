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

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.valaphee.flow.loop.ForEach
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.system.measureNanoTime

/**
 * @author Kevin Ludwig
 */
class Test {
    @Test
    fun `test branch`() {
        val flow = objectMapper.readValue<List<Node>>(
            """
                [
                    {
                        "type" : "com.valaphee.flow.Value",
                        "value" : "true",
                        "out" : "bed769ff-2c95-4d06-80e1-603e1a06ba89"
                    },
                    {
                        "type" : "com.valaphee.flow.control.Branch",
                        "in" : "bed769ff-2c95-4d06-80e1-603e1a06ba89",
                        "when" : {
                            "true" : "162953bc-7789-40d0-82b2-713631f7e65c"
                        }
                    },
                    {
                        "type" : "com.valaphee.flow.Plug",
                        "in" : "162953bc-7789-40d0-82b2-713631f7e65c"
                    }
                ]
            """.trimIndent()
        )
        flow.forEach { coroutineContext.launch { it.bind() } }
        val plug = flow.filterIsInstance<Plug>().single().`in`
        repeat(10) { runBlocking { println("Branch #$it " + measureNanoTime { plug.get() }) } }
    }

    @Test
    fun `test select`() {
        val flow = objectMapper.readValue<List<Node>>(
            """
                [
                    {
                        "type" : "com.valaphee.flow.Value",
                        "value" : "true",
                        "out" : "1e0d2153-608c-4532-a3a0-bff9d9f39cb8"
                    },
                    {
                        "type" : "com.valaphee.flow.Value",
                        "value" : "false",
                        "out" : "e148e38f-85d1-40d2-93ae-3c3660a3b1f9"
                    },
                    {
                        "type" : "com.valaphee.flow.control.Select",
                        "in" : "1e0d2153-608c-4532-a3a0-bff9d9f39cb8",
                        "value" : {
                            "true" : "e148e38f-85d1-40d2-93ae-3c3660a3b1f9"
                        },
                        "out" : "46c52276-3478-41aa-a5a4-3d01acaccaf3"
                    },
                    {
                        "type" : "com.valaphee.flow.Plug",
                        "in" : "46c52276-3478-41aa-a5a4-3d01acaccaf3"
                    }
                ]
            """.trimIndent()
        )
        flow.forEach { coroutineContext.launch { it.bind() } }
        val plug = flow.filterIsInstance<Plug>().single().`in`
        repeat(10) { runBlocking { println("Select #$it " + measureNanoTime { plug.get() }) } }
    }

    @Test
    fun `test foreach`() {
        val flow = objectMapper.readValue<List<Node>>(
            """
                [
                    {
                        "type" : "com.valaphee.flow.Value",
                        "value" : [ 1, 2, 3, 4, 5 ],
                        "out" : "4e753d42-7f3b-41d3-a6e5-dbecb0e31b55"
                    },
                    {
                        "type" : "com.valaphee.flow.loop.ForEach",
                        "in" : "4e753d42-7f3b-41d3-a6e5-dbecb0e31b55",
                        "body" : "f67440a8-4376-4122-8426-8c93b9ee84ef",
                        "exit" : "1fdc959a-40e5-47f4-b928-a0a4392b2be5"
                    },
                    {
                        "type" : "com.valaphee.flow.Plug",
                        "in" : "f67440a8-4376-4122-8426-8c93b9ee84ef"
                    },
                    {
                        "type" : "com.valaphee.flow.Plug",
                        "in" : "1fdc959a-40e5-47f4-b928-a0a4392b2be5"
                    }
                ]
            """.trimIndent()
        )
        flow.forEach { coroutineContext.launch { it.bind() } }
        val forEach = flow.filterIsInstance<ForEach>().single()
        coroutineContext.launch { while (true) println(forEach.body.get()) }
        runBlocking { forEach.exit.get() }
    }

    companion object {
        private val coroutineContext = CoroutineScope(Dispatchers.Default)
        private val objectMapper = jacksonObjectMapper()
    }
}
