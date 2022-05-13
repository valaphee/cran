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

package com.valaphee.flow.control

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.valaphee.flow.DataPath
import com.valaphee.flow.Node
import com.valaphee.flow.util.DataPlug
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
open class SelectBenchmark {
    lateinit var value: DataPath

    @Setup(Level.Trial)
    fun setup() {
        val flow = jacksonObjectMapper().readValue<List<Node>>(
            """
                [
                    {
                        "type": "com.valaphee.flow.Value",
                        "value": "true",
                        "out": 0
                    },
                    {
                        "type": "com.valaphee.flow.Value",
                        "value": "false",
                        "out": 1
                    },
                    {
                        "type": "com.valaphee.flow.control.Select",
                        "in": 0,
                        "in_value": {
                            "true": 1
                        },
                        "in_default": 2,
                        "out": 3
                    },
                    {
                        "type": "com.valaphee.flow.util.DataPlug",
                        "aux": 3
                    }
                ]
            """.trimIndent()
        )
        flow.forEach { it.initialize() }

        value = flow.filterIsInstance<DataPlug>().single().aux
    }

    @Benchmark
    fun execute() {
        runBlocking { value.get() }
    }
}
