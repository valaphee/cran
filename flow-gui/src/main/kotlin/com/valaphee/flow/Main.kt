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

package com.valaphee.flow

import com.fasterxml.jackson.module.kotlin.readValue
import com.valaphee.flow.graph.Graph
import com.valaphee.flow.graph.SkinFactory
import com.valaphee.flow.meta.Meta
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.geometry.Side
import javafx.scene.control.ContextMenu
import javafx.scene.control.ListCell
import javafx.scene.control.SplitPane
import javafx.scene.control.TabPane
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.scene.text.Font
import javafx.stage.FileChooser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tornadofx.App
import tornadofx.FileChooserMode
import tornadofx.View
import tornadofx.action
import tornadofx.bindSelected
import tornadofx.chooseFile
import tornadofx.contextmenu
import tornadofx.customitem
import tornadofx.dynamicContent
import tornadofx.importStylesheet
import tornadofx.item
import tornadofx.launch
import tornadofx.listview
import tornadofx.menu
import tornadofx.menubar
import tornadofx.onChange
import tornadofx.pane
import tornadofx.scrollpane
import tornadofx.separator
import tornadofx.splitpane
import tornadofx.style
import tornadofx.tab
import tornadofx.tabpane
import tornadofx.textarea
import tornadofx.textfield
import tornadofx.toObservable
import tornadofx.toProperty
import tornadofx.vbox
import tornadofx.vgrow
import kotlin.system.exitProcess

/**
 * @author Kevin Ludwig
 */
class Main : View("Flow") {
    private val jsonProperty = "".toProperty()

    private val graphsProperty = SimpleListProperty(mutableListOf<Graph>().toObservable())
    private val graphProperty = SimpleObjectProperty<Graph>().apply {
        onChange {
            title = "http://localhost:8080/${(it?.meta?.name ?: it?.id)?.let { " - $it" } ?: ""}"
            jsonProperty.set(it?.let { ObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(it) } ?: "")
        }
    }

    // setSkinFactories(SkinFactory(parent))
    override val root = vbox {
        // Properties
        setPrefSize(1000.0, 800.0)

        // Children
        menubar {
            menu("File") {
                item("Open") {
                    action {
                        chooseFile(filters = arrayOf(FileChooser.ExtensionFilter("All Files", "*.*"))).singleOrNull()?.let {
                            val graph = ObjectMapper.readValue<Graph>(it)
                            graphsProperty += graph
                            graphProperty.set(graph)
                        }
                    }
                }
                item("Save As") { action { chooseFile(filters = arrayOf(FileChooser.ExtensionFilter("All Files", "*.*")), mode = FileChooserMode.Save).singleOrNull()?.let { ObjectMapper.writeValue(it, graphProperty.get()) } } }
                separator()
                item("Exit") { action { close() } }
            }
            menu("Help") { item("About") { action { find<About>().openModal(resizable = false) } } }
        }
        splitpane {
            // Parent Properties
            vgrow = Priority.ALWAYS
            setDividerPositions(0.25)

            // Children
            listview(graphsProperty) {
                // Parent Properties
                SplitPane.setResizableWithParent(this, false)

                // Value
                bindSelected(graphProperty)

                // Properties
                setCellFactory {
                    object : ListCell<Graph>() {
                        init {
                            setOnMouseClicked {
                                if (isEmpty) selectionModel.clearSelection()

                                it.consume()
                            }
                        }

                        override fun updateItem(item: Graph?, empty: Boolean) {
                            super.updateItem(item, empty)

                            text = if (empty || item == null) "" else item.meta?.name ?: item.id.toString()
                        }
                    }
                }

                fun contextMenu(selectedComponents: ObservableList<out Graph>) = ContextMenu().apply {
                    if (selectedComponents.isEmpty()) item("New Graph") { action { graphsProperty.value += Graph() } }
                    else item("Delete") {
                        action {
                            ApiScope.launch {
                                delete(selectedComponents)
                                this@Main.refresh()
                            }
                        }
                    }
                }

                contextMenu = contextMenu(selectionModel.selectedItems)
                selectionModel.selectedItems.onChange { contextMenu = contextMenu(it.list) }

                // Events
                setOnKeyPressed {
                    when (it.code) {
                        KeyCode.DELETE -> if (!selectionModel.isEmpty) {
                            ApiScope.launch {
                                delete(selectionModel.selectedItems)
                                this@Main.refresh()
                            }
                            it.consume()
                        }
                        KeyCode.F5 -> {
                            ApiScope.launch { this@Main.refresh() }
                            it.consume()
                        }
                        else -> Unit
                    }
                }

                // Initialization
                ApiScope.launch { this@Main.refresh() }
            }
            tabpane {
                vgrow = Priority.ALWAYS

                // Properties
                side = Side.BOTTOM
                tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

                // Children
                tab("Graph") {
                    scrollpane {
                        pane { dynamicContent(graphProperty) { it?.flow?.setSkinFactories(SkinFactory(this)) } }
                        contextMenu = contextmenu {
                            val searchProperty = "".toProperty()
                            lateinit var search: TextField
                            customitem(hideOnClick = false) { search = textfield { textProperty().onChange { searchProperty.set(it) } } }
                            setOnKeyPressed { search.requestFocus() }

                            val items = items.toMutableList()
                            Spec.nodes.forEach {
                                item(it.name) {
                                    action {
                                        val local = sceneToLocal(x, y)
                                        graphProperty.get().newNode(it, Meta.Node(local.x, local.y))
                                    }
                                }
                            }
                            searchProperty.onChange { search ->
                                this.items.setAll(items)
                                (if (search!!.isEmpty()) Spec.nodes else Spec.nodes.filter { it.name.contains(search, true) }).forEach {
                                    item(it.name) {
                                        action {
                                            val local = sceneToLocal(x, y)
                                            graphProperty.get().newNode(it, Meta.Node(local.x, local.y))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                tab("JSON") {
                    // Children
                    textarea(jsonProperty) { style { font = Font.font("monospaced", 10.0) } }

                    // Events
                    setOnSelectionChanged { jsonProperty.set(ObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(graphProperty.get())) }
                }
            }
        }

        // Events
        setOnKeyPressed {
            when (it.code) {
                KeyCode.S -> if (it.isControlDown) {
                    graphProperty.get()?.let {
                        ApiScope.launch {
                            delete(listOf(it))
                            launch {
                                if (HttpClient.post("http://localhost:8080/v1/graph") {
                                        contentType(ContentType.Application.Json)
                                        setBody(it)
                                    }.status == HttpStatusCode.OK) {
                                    refresh()
                                }
                            }
                        }
                    }
                }
                else -> Unit
            }
        }
    }

    private suspend fun refresh() {
        HttpClient.get("http://localhost:8080/v1/graph/").body<List<Graph>>().also {
            withContext(Dispatchers.Main) {
                val id = graphProperty.get()?.id
                graphsProperty.setAll(it)
                graphProperty.set(it.singleOrNull { it.id == id })
            }
        }
    }

    private suspend fun delete(graphs: List<Graph>) {
        graphs.map { coroutineScope { launch { HttpClient.delete("http://localhost:8080/v1/graph/${it.id}") } } }.joinAll()
    }
}

/**
 * @author Kevin Ludwig
 */
class MainApp : App(Image(MainApp::class.java.getResourceAsStream("/app.png")), Main::class, Style::class) {
    override fun init() {
        importStylesheet("/style.css")
    }
}

fun main(arguments: Array<String>) {
    SvgImageLoaderFactory.install()

    launch<MainApp>(arguments)

    exitProcess(0)
}
