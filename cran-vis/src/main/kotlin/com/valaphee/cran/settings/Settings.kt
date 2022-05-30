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

package com.valaphee.cran.settings

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import tornadofx.ItemViewModel
import tornadofx.getValue
import tornadofx.setValue
import tornadofx.toProperty
import java.io.File

/**
 * @author Kevin Ludwig
 */
class Settings(
    gridX: Int = 5,
    gridY: Int = 5
) {
    @get:JsonIgnore internal val gridXProperty = gridX.toProperty()
    @get:JsonIgnore internal val gridYProperty = gridY.toProperty()

    var gridX by gridXProperty
    var gridY by gridYProperty

    class Model(
        settings: Settings
    ): ItemViewModel<Settings>(settings) {
        private val objectMapper by di<ObjectMapper>()

        init {
            bind(Settings::gridXProperty)
            bind(Settings::gridYProperty)
        }

        override fun onCommit() {
            objectMapper.writeValue(File(File(System.getProperty("user.home"), ".valaphee/cran").also(File::mkdirs), "settings.json"), item)
        }
    }
}
