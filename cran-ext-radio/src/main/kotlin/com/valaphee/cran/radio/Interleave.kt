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

package com.valaphee.cran.radio

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.cran.Scope
import com.valaphee.cran.node.Arr
import com.valaphee.cran.node.Node
import com.valaphee.cran.spec.In
import com.valaphee.cran.spec.NodeType
import com.valaphee.cran.spec.Out

/**
 * @author Kevin Ludwig
 */
@NodeType("Radio/Interleave")
class Interleave(
    type: String,
    @get:In ("Re", Arr) @get:JsonProperty("in_re") val inRe: Int,
    @get:In ("Im", Arr) @get:JsonProperty("in_im") val inIm: Int,
    @get:Out(""  , Arr) @get:JsonProperty("out"  ) val out : Int,
) : Node(type) {
    override fun initialize(scope: Scope) {
        val inRe = scope.dataPath(inRe)
        val inIm = scope.dataPath(inIm)
        val out = scope.dataPath(out)

        out.set { interleave(inRe.getOfType(), inIm.getOfType()) }
    }

    companion object {
        fun interleave(vararg value: FloatArray): FloatArray {
            val size = value[0].size
            check(value.all { it.size == size })
            val count = value.size - 1

            var n = 0
            var i = 0
            return FloatArray(value.size * size) {
                if (n == count) {
                    n = 0
                    i++
                }
                value[n++][i]
            }
        }
    }
}
