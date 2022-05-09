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

package com.valaphee.flow.loop

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.valaphee.flow.ControlPath
import com.valaphee.flow.Node
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown
import org.openjdk.jmh.annotations.Warmup
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * @author Kevin Ludwig
 */
@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 2, time = 5)
@Fork(1)
open class ForEachBenchmark {
    lateinit var executorService: ExecutorService
    lateinit var begin: ControlPath
    lateinit var end: ControlPath

    @Setup(Level.Trial)
    fun setup() {
        executorService = Executors.newSingleThreadExecutor()
        val scope = CoroutineScope(executorService.asCoroutineDispatcher())

        val flow = jacksonObjectMapper().readValue<List<Node>>(
            """
                [
                    {
                        "type" : "com.valaphee.flow.util.ControlPlug",
                        "aux" : 0
                    },
                    {
                        "type" : "com.valaphee.flow.Value",
                        "value" : [ 1, 2, 3, 4, 5 ],
                        "out" : 1
                    },
                    {
                        "type" : "com.valaphee.flow.loop.ForEach",
                        "in" : 0,
                        "in_value" : 1,
                        "out_body" : 2,
                        "out" : 3
                    },
                    {
                        "type" : "com.valaphee.flow.util.ControlPlug",
                        "aux" : 2
                    },
                    {
                        "type" : "com.valaphee.flow.util.ControlPlug",
                        "aux" : 3
                    }
                ]
            """.trimIndent()
        )
        flow.forEach { it.run(scope) }

        val forEach = flow.filterIsInstance<ForEach>().single()
        forEach.outBody.collect(scope) {}
        begin = forEach.`in`
        end = forEach.`out`

    }

    @Benchmark
    fun execute() {
        runBlocking {
            begin.emit()
            end.wait()
        }
    }

    @TearDown
    fun tearDown() {
        executorService.shutdown()
    }
}
