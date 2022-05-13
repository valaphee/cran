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
import com.valaphee.flow.control.ForEach
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Threads
import org.openjdk.jmh.annotations.Warmup
import java.util.concurrent.TimeUnit

/**
 * @author Kevin Ludwig
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 2, time = 1)
@Threads(2)
@Fork(1)
open class ForEachBenchmark {
    lateinit var begin: ControlPath

    @Setup(Level.Trial)
    fun setup() {
        val flow = jacksonObjectMapper().readValue<List<Node>>(
            """
                [
                    {
                        "type": "com.valaphee.flow.util.ControlPlug",
                        "aux": 0
                    },
                    {
                        "type": "com.valaphee.flow.Value",
                        "value": [ 1, 2, 3, 4, 5 ],
                        "out": 1
                    },
                    {
                        "type": "com.valaphee.flow.control.ForEach",
                        "in": 0,
                        "in_value": 1,
                        "out_value": 2,
                        "out_body": 3,
                        "out": 4
                    },
                    {
                        "type": "com.valaphee.flow.util.ControlPlug",
                        "aux": 3
                    },
                    {
                        "type": "com.valaphee.flow.util.ControlPlug",
                        "aux": 4
                    }
                ]
            """.trimIndent()
        )
        flow.forEach { it.initialize() }

        begin = flow.filterIsInstance<ForEach>().single().`in`
    }

    @Benchmark
    fun execute() {
        runBlocking { begin() }
    }
}
