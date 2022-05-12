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
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.SplitPane
import javafx.scene.control.TabPane
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.stage.FileChooser
import jfxtras.labs.util.event.MouseControlUtil
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
import tornadofx.imageview
import tornadofx.importStylesheet
import tornadofx.item
import tornadofx.launch
import tornadofx.listview
import tornadofx.menu
import tornadofx.menubar
import tornadofx.onChange
import tornadofx.pane
import tornadofx.rectangle
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
                item("Import") {
                    action {
                        chooseFile(filters = arrayOf(FileChooser.ExtensionFilter("Flow", "*.flw"), FileChooser.ExtensionFilter("All Files", "*.*"))).singleOrNull()?.let {
                            val graph = ObjectMapper.readValue<Graph>(it)
                            graphsProperty += graph
                            graphProperty.set(graph)
                        }
                    }
                }
                item("Export As...") { action { chooseFile(filters = arrayOf(FileChooser.ExtensionFilter("Flow", "*.flw"), FileChooser.ExtensionFilter("All Files", "*.*")), mode = FileChooserMode.Save).singleOrNull()?.let { ObjectMapper.writeValue(it, graphProperty.get()) } } }
                separator()
                item("Exit") { action { close() } }
            }
            menu("Help") { item("About") { action { find<About>().openModal(resizable = false) } } }
        }
        /*hbox {
            // Parent Properties
            vgrow = Priority.ALWAYS*/

        // Children
        splitpane {
            // Parent Properties
            /*hgrow = Priority.ALWAYS*/
            vgrow = Priority.ALWAYS

            // Properties
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

                            text = if (empty || item == null) "" else item.meta.name
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

                // Initialization
                ApiScope.launch { this@Main.refresh() }
            }
            tabpane {
                // Properties
                side = Side.BOTTOM
                tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

                // Children
                tab("Graph") {
                    scrollpane {
                        pane {
                            // Properties
                            /*isFitToHeight = true
                            isFitToWidth = true*/

                            // Children
                            dynamicContent(graphProperty) { it?.flow?.setSkinFactories(SkinFactory(this)) }

                            // Events
                            MouseControlUtil.addSelectionRectangleGesture(this, rectangle {
                                stroke = Color.rgb(255, 255, 255, 1.0)
                                fill = Color.rgb(0, 0, 0, 0.5)
                            })
                        }

                        contextMenu = contextmenu {
                            val searchProperty = "".toProperty()
                            lateinit var search: TextField
                            customitem(hideOnClick = false) { search = textfield { textProperty().onChange { searchProperty.set(it) } } }
                            setOnKeyPressed { search.requestFocus() }

                            val treeItems = mutableListOf<MenuItem>()
                            val nodeItems = mutableMapOf<String, MenuItem>()
                            Spec.nodes.forEach { node ->
                                val name = node.name.split('/')
                                val path = StringBuilder()
                                var item: MenuItem? = null
                                name.forEachIndexed { i, element ->
                                    path.append("${element}/")
                                    val icon = Manifest.nodes[path.toString().removeSuffix("/")]?.let { this::class.java.getResourceAsStream(it.icon)?.let { imageview(Image(it, 16.0, 16.0, false, false)) } }
                                    item = when (val _item = item) {
                                        null -> treeItems.find { it.text == element } ?: if (i == name.lastIndex) MenuItem(element, icon).apply {
                                            treeItems += this
                                            nodeItems[node.name] = this

                                            action {
                                                val local = sceneToLocal(x - currentWindow!!.x, y - currentWindow!!.y)
                                                graphProperty.get().newNode(node, Meta.Node(local.x, local.y))
                                            }
                                        } else Menu(element, icon).apply { treeItems += this }
                                        is Menu -> _item.items.find { it.text == element } ?: if (i == name.lastIndex) MenuItem(element, icon).apply {
                                            _item.items += this
                                            nodeItems[node.name] = this

                                            action {
                                                val local = sceneToLocal(x - currentWindow!!.x, y - currentWindow!!.y)
                                                graphProperty.get().newNode(node, Meta.Node(local.x, local.y))
                                            }
                                        } else Menu(element, icon).apply { _item.items += this }
                                        else -> error("")
                                    }
                                }
                            }

                            val items = items.toMutableList()
                            this.items.addAll(treeItems)
                            searchProperty.onChange { _search ->
                                this.items.setAll(items)
                                this.items.addAll(if (_search!!.isEmpty()) treeItems else nodeItems.filterKeys { it.contains(_search, true) }.values)
                            }
                        }
                    }
                }
                tab("JSON") {
                    // Children
                    textarea(jsonProperty) { style { font = Font.font("monospaced", 10.0) } }

                    // Events
                    setOnSelectionChanged { jsonProperty.set(graphProperty.get()?.let { ObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(it) } ?: "") }
                }
            }
            /*}
            drawer(Side.RIGHT)*/
        }

        // Events
        setOnKeyPressed {
            if (it.isControlDown) when (it.code) {
                KeyCode.C -> {
                    graphProperty.get()?.let { graph -> graph.flow.nodes.filter { it.isSelected } }
                    it.consume()
                }
                KeyCode.S -> {
                    graphProperty.get()?.let { graph ->
                        ApiScope.launch {
                            delete(listOf(graph))
                            launch {
                                if (HttpClient.post("http://localhost:8080/v1/graph") {
                                        contentType(ContentType.Application.Json)
                                        setBody(graph)
                                    }.status == HttpStatusCode.OK) {
                                    refresh()
                                }
                            }
                        }
                    }
                    it.consume()
                }
                KeyCode.V -> Unit
                else -> Unit
            } else when (it.code) {
                KeyCode.DELETE -> {
                    graphProperty.get()?.let { it.flow.nodes.forEach { if (it.isSelected) it.flow.remove(it) } }
                    it.consume()
                }
                KeyCode.F5 -> {
                    ApiScope.launch { this@Main.refresh() }
                    it.consume()
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
