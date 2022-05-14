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

package com.valaphee.flow.math.scalar

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.DataPath
import com.valaphee.flow.DataPathException
import com.valaphee.flow.StatelessNode
import com.valaphee.flow.spec.DataType
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.Node
import com.valaphee.flow.spec.Out
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.absoluteValue

/**
 * @author Kevin Ludwig
 */
@Node("Math/Scalar/Absolute")
class Absolute(
    @get:In ("X"  , DataType.Num, "") @get:JsonProperty("in" ) val `in`: DataPath,
    @get:Out("|X|", DataType.Num    ) @get:JsonProperty("out") val out : DataPath
) : StatelessNode() {
    override fun initialize() {
        out.set {
            val `in` = `in`.get()
            when (`in`) {
                is Byte       -> `in`.toInt().absoluteValue
                is Short      -> `in`.toInt().absoluteValue
                is Int        -> `in`.absoluteValue
                is Long       -> `in`.absoluteValue
                is BigInteger -> `in`.abs()
                is Float      -> `in`.absoluteValue
                is Double     -> `in`.absoluteValue
                is BigDecimal -> `in`.abs()
                else          -> DataPathException.invalidTypeInExpression("|$`in`|")
            }
        }
    }
}
