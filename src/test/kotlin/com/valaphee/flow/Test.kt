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
import com.valaphee.flow.control.BranchBenchmark
import com.valaphee.flow.control.ForBenchmark
import com.valaphee.flow.control.SelectBenchmark
import com.valaphee.flow.util.ControlPlug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

/**
 * @author Kevin Ludwig
 */
class Test {
    @Test
    fun `test branch`() {
        val benchmark = BranchBenchmark()
        benchmark.init()
        runBlocking { repeat(10) { println("Branch #$it " + measureNanoTime {  benchmark.execute() }) } }
    }

    @Test
    fun `test select`() {
        val benchmark = SelectBenchmark()
        benchmark.init()
        runBlocking { repeat(10) { println("Select #$it " + measureNanoTime {  benchmark.execute() }) } }
    }

    @Test
    fun `test for`() {
        val benchmark = ForBenchmark()
        benchmark.init()
        runBlocking { repeat(10) { println("For #$it " + measureNanoTime {  benchmark.execute() }) } }
    }

    @Test
    fun `test foreach`() {
        val benchmark = ForBenchmark()
        benchmark.init()
        runBlocking { repeat(10) { println("ForEach #$it " + measureNanoTime {  benchmark.execute() }) } }
    }

    @Test
    fun `generate fibonacci sequence`() {
        val flow = jacksonObjectMapper().readValue<List<Node>>(
            """
                [
                    {
                        "type" : "com.valaphee.flow.util.ControlPlug",
                        "aux" : 0
                    },
                    {
                        "type" : "com.valaphee.flow.Value",
                        "value" : 0,
                        "out" : 1
                    },
                    {
                        "type" : "com.valaphee.flow.Value",
                        "value" : 1,
                        "out" : 2
                    },
                    {
                        "type" : "com.valaphee.flow.Value",
                        "value" : [ 1, 0 ],
                        "out" : 3
                    },
                    {
                        "type" : "com.valaphee.flow.list.ListGet",
                        "in_list" : 3,
                        "in_index" : 1,
                        "out" : 4
                    },
                    {
                        "type" : "com.valaphee.flow.list.ListGet",
                        "in_list" : 3,
                        "in_index" : 2,
                        "out" : 5
                    },
                    {
                        "type" : "com.valaphee.flow.math.Add",
                        "in_a" : 4,
                        "in_b" : 5,
                        "out" : 6
                    },
                    {
                        "type" : "com.valaphee.flow.list.ListAdd",
                        "in" : 0,
                        "in_list" : 3,
                        "in_item" : 6,
                        "out" : 7
                    },
                    {
                        "type" : "com.valaphee.flow.util.ControlPlug",
                        "aux" : 7
                    }
                ]
            """.trimIndent()
        )
        flow.forEach { it.run(CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())) }

        val plugs = flow.filterIsInstance<ControlPlug>().map { it.aux }
        val inPlug = plugs.single { it.id == 0 }
        val outPlug = plugs.single { it.id == 7 }
        val listValue = flow.filterIsInstance<Value>().single { it.out.id == 3 }
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
}
