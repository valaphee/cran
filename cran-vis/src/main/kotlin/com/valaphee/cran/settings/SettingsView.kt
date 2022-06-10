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

import javafx.beans.value.ObservableValue
import javafx.scene.layout.Priority
import tornadofx.View
import tornadofx.ViewModel
import tornadofx.action
import tornadofx.button
import tornadofx.buttonbar
import tornadofx.enableWhen
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.get
import tornadofx.textfield
import tornadofx.vbox
import tornadofx.vgrow

/**
 * @author Kevin Ludwig
 */
class SettingsView : View("%settings") {
    private val settings by di<Settings>()

    override val root = vbox {
        prefWidth = 600.0
        prefHeight = 800.0

        styleClass += "background"
        stylesheets += "/dark_theme.css"

        val viewModel = ViewModel()

        form {
            vgrow = Priority.ALWAYS

            fieldset {
                field("Grid") {
                    textfield(viewModel.bind { settings.gridXProperty } as ObservableValue<Int>)
                    textfield(viewModel.bind { settings.gridYProperty } as ObservableValue<Int>)
                }
            }
        }
        buttonbar {
            button(messages["settings.ok"]) {
                isDefaultButton = true

                action {
                    viewModel.commit()
                    close()
                }
            }
            button(messages["settings.cancel"]) {
                isCancelButton = true

                action { close() }
            }
            button(messages["settings.apply"]) {
                enableWhen(viewModel.dirty)

                action { viewModel.commit() }
            }
        }
    }

    init {
        title = messages["settings"]
    }
}
