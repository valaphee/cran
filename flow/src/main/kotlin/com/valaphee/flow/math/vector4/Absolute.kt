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

package com.valaphee.flow.math.vector4

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.flow.DataPath
import com.valaphee.flow.DataPathException
import com.valaphee.flow.StatelessNode
import com.valaphee.flow.spec.In
import com.valaphee.flow.spec.Node
import com.valaphee.flow.spec.Out
import com.valaphee.flow.spec.Vec4
import com.valaphee.foundry.math.Double4
import com.valaphee.foundry.math.Float4
import com.valaphee.foundry.math.Int4
import kotlin.math.absoluteValue

/**
 * @author Kevin Ludwig
 */
@Node("Math/Vector 4/Absolute")
class Absolute(
    @get:In ("X"  , Vec4, "") @get:JsonProperty("in" ) val `in`: DataPath,
    @get:Out("|X|", Vec4    ) @get:JsonProperty("out") val out : DataPath
) : StatelessNode() {
    override fun initialize() {
        out.set {
            val `in` = `in`.get()
            when (`in`) {
                is Int4    -> `in`.abs()
                is Float4  -> `in`.abs()
                is Double4 -> `in`.abs()
                else       -> DataPathException.invalidTypeInExpression("|$`in`|")
            }
        }
    }

    companion object {
        private fun Int4.abs() = Int4(x.absoluteValue, y.absoluteValue, z.absoluteValue, w.absoluteValue)

        private fun Float4.abs() = Float4(x.absoluteValue, y.absoluteValue, z.absoluteValue, w.absoluteValue)

        private fun Double4.abs() = Double4(x.absoluteValue, y.absoluteValue, z.absoluteValue, w.absoluteValue)
    }
}
