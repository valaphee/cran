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

package com.valaphee.cran.node.math.scalar

import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.cran.node.Num
import com.valaphee.cran.node.UnaryOperation
import com.valaphee.cran.spec.In
import com.valaphee.cran.spec.NodeDecl
import com.valaphee.cran.spec.Out

/**
 * @author Kevin Ludwig
 */
@NodeDecl("Math/Scalar/Absolute")
class Absolute(
    type: String,
    @get:In ("X"  , Num) @get:JsonProperty("in" ) override val `in`: Int,
    @get:Out("|X|", Num) @get:JsonProperty("out") override val out : Int
) : UnaryOperation(type)
