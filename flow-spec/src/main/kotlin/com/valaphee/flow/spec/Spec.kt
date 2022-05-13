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

package com.valaphee.flow.spec

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author Kevin Ludwig
 */
class Spec(
    @get:JsonProperty("nodes") val nodes: List<Node>
) {
    class Node(
        @get:JsonProperty("name" ) val name : String    ,
        @get:JsonProperty("ports") val ports: List<Port>,
        @get:JsonProperty("json" ) val json : String
    ) {
        class Port(
            @get:JsonProperty("name"     ) val name    : String ,
            @get:JsonProperty("type"     ) val type    : Type   ,
            @get:JsonProperty("data_type") val dataType: String ,
            @get:JsonProperty("json"     ) val json    : String ,
        ) {
            enum class Type {
                @JsonProperty( "in_control")  InControl,
                @JsonProperty("out_control") OutControl,
                @JsonProperty( "in_data"   )  InData   ,
                @JsonProperty("out_data"   ) OutData   ,
                @JsonProperty(    "const"  )    Const
            }
        }
    }
}
