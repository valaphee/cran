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

import com.valaphee.cran.graph.VGraphDefault
import javafx.scene.control.TreeItem
import tornadofx.View
import tornadofx.action
import tornadofx.button
import tornadofx.buttonbar
import tornadofx.textfield
import tornadofx.vbox

/**
 * @author Kevin Ludwig
 */
class RenameView(
    private val selectedValue: TreeItem<Pair<String, VGraphDefault?>>
) : View() {
    override val root = vbox {
        styleClass += "background"
        stylesheets += "/dark_theme.css"

        val name = textfield(selectedValue.value.first)

        buttonbar {
            button("Ok") {
                isDefaultButton = true

                action {
                    close()

                    var path = mutableListOf<String>(name.text).apply {
                        var parent = selectedValue.parent
                        while (parent != null) {
                            if (parent.parent != null) this += parent.value.first
                            parent = parent.parent
                        }
                        reverse()
                    }.joinToString("/")

                    selectedValue.value = name.text to selectedValue.value.second

                    selectedValue.value.second?.name = path
                    val children = selectedValue.children.toMutableList()
                    while (children.isNotEmpty()) {
                        val child = children.removeLast()
                        path += "/${child.value.first}"
                        child.value.second?.name = path
                        children += child.children
                    }
                }
            }
            button("Cancel") {
                isCancelButton = true

                action { close() }
            }
        }
    }
}
