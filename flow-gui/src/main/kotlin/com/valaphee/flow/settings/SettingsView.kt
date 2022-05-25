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

package com.valaphee.flow.settings

import tornadofx.View
import tornadofx.action
import tornadofx.button
import tornadofx.buttonbar
import tornadofx.enableWhen
import tornadofx.vbox

/**
 * @author Kevin Ludwig
 */
class SettingsView : View("Settings") {
    private val settings by di<Settings>()
    private val settingsModel = Settings.Model(settings)

    override val root = vbox {
        // Properties
        setPrefSize(800.0, 600.0)

        // Children
        buttonbar {
            button("Ok") {
                action {
                    settingsModel.commit()
                    close()
                }
            }
            button("Cancel") { action { close() } }
            button("Apply") {
                enableWhen(settingsModel.dirty)
                action { settingsModel.commit() }
            }
        }
    }
}
