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
import com.valaphee.flow.loop.For
import com.valaphee.flow.loop.ForEach
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.UUID
import java.util.concurrent.Executors
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

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
                        "type" : "com.valaphee.flow.ControlPlug",
                        "aux" : "1a607868-cd06-49a1-a8f8-65ddf450bf42"
                    },
                    {
                        "type" : "com.valaphee.flow.Value",
                        "value" : "true",
                        "out" : "bed769ff-2c95-4d06-80e1-603e1a06ba89"
                    },
                    {
                        "type" : "com.valaphee.flow.control.Branch",
                        "in" : "1a607868-cd06-49a1-a8f8-65ddf450bf42",
                        "in_value" : "bed769ff-2c95-4d06-80e1-603e1a06ba89",
                        "out" : {
                            "true" : "162953bc-7789-40d0-82b2-713631f7e65c"
                        }
                    },
                    {
                        "type" : "com.valaphee.flow.ControlPlug",
                        "aux" : "162953bc-7789-40d0-82b2-713631f7e65c"
                    }
                ]
            """.trimIndent()
        )
        flow.forEach { it.run(coroutineScope) }

        val plugs = flow.filterIsInstance<ControlPlug>().map { it.aux }
        val inPlug = plugs.single { it.id == UUID.fromString("1a607868-cd06-49a1-a8f8-65ddf450bf42") }
        val outPlug = plugs.single { it.id == UUID.fromString("162953bc-7789-40d0-82b2-713631f7e65c") }
        repeat(10) {
            runBlocking {
                println("Branch #$it " + measureNanoTime {
                    inPlug.emit()
                    outPlug.wait()
                })
            }
        }
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
                        "in_value" : {
                            "true" : "e148e38f-85d1-40d2-93ae-3c3660a3b1f9"
                        },
                        "out" : "46c52276-3478-41aa-a5a4-3d01acaccaf3"
                    },
                    {
                        "type" : "com.valaphee.flow.DataPlug",
                        "aux" : "46c52276-3478-41aa-a5a4-3d01acaccaf3"
                    }
                ]
            """.trimIndent()
        )
        flow.forEach { it.run(coroutineScope) }

        val plug = flow.filterIsInstance<DataPlug>().single().aux
        repeat(10) { runBlocking { println("Select #$it " + measureNanoTime { plug.get() }) } }
    }

    @Test
    fun `test for`() {
        val flow = objectMapper.readValue<List<Node>>(
            """
                [
                    {
                        "type" : "com.valaphee.flow.ControlPlug",
                        "aux" : "05c40872-b49e-4675-9e78-15a552a4941f"
                    },
                    {
                        "type" : "com.valaphee.flow.Value",
                        "value" : 0,
                        "out" : "4e753d42-7f3b-41d3-a6e5-dbecb0e31b55"
                    },
                    {
                        "type" : "com.valaphee.flow.Value",
                        "value" : 1,
                        "out" : "4e753d42-7f3b-41d3-a6e5-dbecb0e31b56"
                    },
                    {
                        "type" : "com.valaphee.flow.Value",
                        "value" : 1000,
                        "out" : "4e753d42-7f3b-41d3-a6e5-dbecb0e31b57"
                    },
                    {
                        "type" : "com.valaphee.flow.loop.For",
                        "in" : "05c40872-b49e-4675-9e78-15a552a4941f",
                        "in_range_start" : "4e753d42-7f3b-41d3-a6e5-dbecb0e31b55",
                        "in_range_end" : "4e753d42-7f3b-41d3-a6e5-dbecb0e31b57",
                        "in_step" : "4e753d42-7f3b-41d3-a6e5-dbecb0e31b56",
                        "out_body" : "f67440a8-4376-4122-8426-8c93b9ee84ef",
                        "out" : "1fdc959a-40e5-47f4-b928-a0a4392b2be5"
                    },
                    {
                        "type" : "com.valaphee.flow.ControlPlug",
                        "aux" : "f67440a8-4376-4122-8426-8c93b9ee84ef"
                    },
                    {
                        "type" : "com.valaphee.flow.ControlPlug",
                        "aux" : "1fdc959a-40e5-47f4-b928-a0a4392b2be5"
                    }
                ]
            """.trimIndent()
        )
        flow.forEach { it.run(coroutineScope) }

        val `for` = flow.filterIsInstance<For>().single()
        `for`.outBody.collect(coroutineScope) {}
        runBlocking {
            println("For " + measureNanoTime {
                `for`.`in`.emit()
                `for`.out.wait()
            })
        }
    }

    @Test
    fun `test foreach`() {
        val flow = objectMapper.readValue<List<Node>>(
            """
                [
                    {
                        "type" : "com.valaphee.flow.ControlPlug",
                        "aux" : "05c40872-b49e-4675-9e78-15a552a4941f"
                    },
                    {
                        "type" : "com.valaphee.flow.Value",
                        "value" : [ 1, 2, 3, 4, 5 ],
                        "out" : "4e753d42-7f3b-41d3-a6e5-dbecb0e31b55"
                    },
                    {
                        "type" : "com.valaphee.flow.loop.ForEach",
                        "in" : "05c40872-b49e-4675-9e78-15a552a4941f",
                        "in_value" : "4e753d42-7f3b-41d3-a6e5-dbecb0e31b55",
                        "out_body" : "f67440a8-4376-4122-8426-8c93b9ee84ef",
                        "out" : "1fdc959a-40e5-47f4-b928-a0a4392b2be5"
                    },
                    {
                        "type" : "com.valaphee.flow.ControlPlug",
                        "aux" : "f67440a8-4376-4122-8426-8c93b9ee84ef"
                    },
                    {
                        "type" : "com.valaphee.flow.ControlPlug",
                        "aux" : "1fdc959a-40e5-47f4-b928-a0a4392b2be5"
                    }
                ]
            """.trimIndent()
        )
        flow.forEach { it.run(coroutineScope) }

        val forEach = flow.filterIsInstance<ForEach>().single()
        forEach.outBody.collect(coroutineScope) {}
        runBlocking {
            println("ForEach " + measureTimeMillis {
                forEach.`in`.emit()
                forEach.out.wait()
            })
        }
    }

    @Test
    fun `generate fibonacci sequence`() {
        val flow = objectMapper.readValue<List<Node>>(
            """
                [
                    {
                        "type" : "com.valaphee.flow.ControlPlug",
                        "aux" : "7e1ac9d3-5b28-4b44-9fd1-a486685ab253"
                    },
                    {
                        "type" : "com.valaphee.flow.Value",
                        "value" : 0,
                        "out" : "94e212b7-cd96-4d07-a945-c6de4fed4cd2"
                    },
                    {
                        "type" : "com.valaphee.flow.Value",
                        "value" : 1,
                        "out" : "ba15541e-26fe-4e1e-8147-9f7a4e6160cb"
                    },
                    {
                        "type" : "com.valaphee.flow.Value",
                        "value" : [ 1, 0 ],
                        "out" : "b5c70241-06d3-4e0c-bd20-6813c2c71442"
                    },
                    {
                        "type" : "com.valaphee.flow.list.ListGet",
                        "in_list" : "b5c70241-06d3-4e0c-bd20-6813c2c71442",
                        "in_index" : "94e212b7-cd96-4d07-a945-c6de4fed4cd2",
                        "out" : "29d14740-7c32-44a2-8495-5a25a51d9b38"
                    },
                    {
                        "type" : "com.valaphee.flow.list.ListGet",
                        "in_list" : "b5c70241-06d3-4e0c-bd20-6813c2c71442",
                        "in_index" : "ba15541e-26fe-4e1e-8147-9f7a4e6160cb",
                        "out" : "d250c37a-4907-4999-89bf-ebdfa4d3b67d"
                    },
                    {
                        "type" : "com.valaphee.flow.math.Add",
                        "in_a" : "29d14740-7c32-44a2-8495-5a25a51d9b38",
                        "in_b" : "d250c37a-4907-4999-89bf-ebdfa4d3b67d",
                        "out" : "408f6589-32c1-4531-857d-1943999320de"
                    },
                    {
                        "type" : "com.valaphee.flow.list.ListAdd",
                        "in" : "7e1ac9d3-5b28-4b44-9fd1-a486685ab253",
                        "in_list" : "b5c70241-06d3-4e0c-bd20-6813c2c71442",
                        "in_item" : "408f6589-32c1-4531-857d-1943999320de",
                        "out" : "966c57a3-06c7-4ee1-8369-52e9db4ae28f"
                    },
                    {
                        "type" : "com.valaphee.flow.ControlPlug",
                        "aux" : "966c57a3-06c7-4ee1-8369-52e9db4ae28f"
                    }
                ]
            """.trimIndent()
        )
        flow.forEach { it.run(coroutineScope) }

        val plugs = flow.filterIsInstance<ControlPlug>().map { it.aux }
        val inPlug = plugs.single { it.id == UUID.fromString("7e1ac9d3-5b28-4b44-9fd1-a486685ab253") }
        val outPlug = plugs.single { it.id == UUID.fromString("966c57a3-06c7-4ee1-8369-52e9db4ae28f") }
        val listValue = flow.filterIsInstance<Value>().single { it.out.id == UUID.fromString("b5c70241-06d3-4e0c-bd20-6813c2c71442") }
        runBlocking {
            println("Fibonacci " + measureTimeMillis {
                repeat(10) {
                    inPlug.emit()
                    outPlug.wait()
                }
            })
            println("Fibonacci " + listValue.value)
        }
    }

    companion object {
        private val coroutineScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
        private val objectMapper = jacksonObjectMapper()
    }
}
