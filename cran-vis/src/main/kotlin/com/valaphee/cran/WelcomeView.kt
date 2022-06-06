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

package com.valaphee.cran

import com.valaphee.cran.settings.Settings
import javafx.scene.Parent
import javafx.scene.layout.Pane
import tornadofx.View
import tornadofx.bind
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.get
import tornadofx.textfield

/**
 * @author Kevin Ludwig
 */
class WelcomeView : View("%welcome") {
    override val root by fxml<Parent>("/welcome.fxml")
    private val welcomePane by fxid<Pane>()

    private val environment = Settings.Environment("localhost:8080")

    init {
        title = messages["welcome"]

        with(welcomePane) {
            form {
                fieldset {
                    field("Target") {
                        textfield("localhost:8080").bind(environment.targetProperty)
                    }
                }
            }
        }
    }

    fun connectButtonAction() {
        close()

        MainView(environment).openWindow(escapeClosesWindow = false, owner = null)?.isMaximized = true
    }

    fun exitButtonAction() {
        close()
    }
}
