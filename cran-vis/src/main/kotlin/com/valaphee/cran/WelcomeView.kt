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
import tornadofx.action
import tornadofx.button
import tornadofx.chooseFile
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.get
import tornadofx.textfield
import java.io.File

/**
 * @author Kevin Ludwig
 */
class WelcomeView : View("%welcome") {
    override val root by fxml<Parent>("/welcome.fxml")
    private val welcomePane by fxid<Pane>()

    private val settings by di<Settings>()

    init {
        title = messages["welcome"]

        with(welcomePane) {
            form {
                fieldset { field("Target") { textfield(settings.environment.targetProperty) } }
                fieldset("TLS") {
                    field("Client Certificate Chain") {
                        textfield(settings.environment.clientCerProperty)
                        button("...") {
                            action {
                                val parentPath = if (settings.environment.clientCer.isEmpty()) null else File(settings.environment.clientCer).parentFile
                                chooseFile("Select Client Certificate Chain", emptyArray(), if (parentPath?.isDirectory == true) parentPath else null).firstOrNull()?.let { settings.environment.clientCer = it.absolutePath }
                            }
                        }
                    }
                    field("Client Private Key") {
                        textfield(settings.environment.clientKeyProperty)
                        button("...") {
                            action {
                                val parentPath = if (settings.environment.clientKey.isEmpty()) null else File(settings.environment.clientKey).parentFile
                                chooseFile("Select Client Certificate Chain", emptyArray(), if (parentPath?.isDirectory == true) parentPath else null).firstOrNull()?.let { settings.environment.clientKey = it.absolutePath }
                            }
                        }
                    }
                    field("Server Certificate") {
                        textfield(settings.environment.serverCerProperty)
                        button("...") {
                            action {
                                val parentPath = if (settings.environment.serverCer.isEmpty()) null else File(settings.environment.serverCer).parentFile
                                chooseFile("Select Client Certificate Chain", emptyArray(), if (parentPath?.isDirectory == true) parentPath else null).firstOrNull()?.let { settings.environment.serverCer = it.absolutePath }
                            }
                        }
                    }
                }
            }
        }
    }

    fun connectButtonAction() {
        replaceWith(MainView(settings.environment), null, true, true)
        primaryStage.isMaximized = true
    }

    fun exitButtonAction() {
        close()
    }
}
