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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Injector
import com.hazelcast.client.HazelcastClient
import com.hazelcast.client.config.ClientConfig
import com.hazelcast.config.SerializerConfig
import com.hazelcast.core.EntryEvent
import com.hazelcast.core.EntryListener
import com.hazelcast.map.MapEvent
import com.hazelcast.nio.ObjectDataInput
import com.hazelcast.nio.ObjectDataOutput
import com.hazelcast.nio.serialization.StreamSerializer
import com.valaphee.cran.graph.Graph
import com.valaphee.cran.graph.GraphWithMetaBase
import com.valaphee.cran.graph.SkinFactory
import com.valaphee.cran.graph.VGraphBase
import com.valaphee.cran.graph.VGraphDefault
import com.valaphee.cran.graph.data.PropertiesView
import com.valaphee.cran.meta.Meta
import com.valaphee.cran.settings.SettingsView
import com.valaphee.cran.spec.Spec
import com.valaphee.cran.spec.SpecLookup
import com.valaphee.cran.util.PathTree
import com.valaphee.cran.util.asStyleClass
import com.valaphee.cran.util.update
import eu.mihosoft.vrl.workflow.VNode
import eu.mihosoft.vrl.workflow.incubating.LayoutGeneratorSmart
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Parent
import javafx.scene.control.Menu
import javafx.scene.control.MenuItem
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextArea
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.control.cell.TextFieldTreeCell
import javafx.scene.input.Clipboard
import javafx.scene.input.DataFormat
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import javafx.util.StringConverter
import jfxtras.labs.util.event.MouseControlUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import tornadofx.FileChooserMode
import tornadofx.View
import tornadofx.action
import tornadofx.chooseFile
import tornadofx.contextmenu
import tornadofx.customitem
import tornadofx.drawer
import tornadofx.dynamicContent
import tornadofx.get
import tornadofx.getValue
import tornadofx.item
import tornadofx.onChange
import tornadofx.rectangle
import tornadofx.removeFromParent
import tornadofx.runLater
import tornadofx.selectedValue
import tornadofx.separator
import tornadofx.setContent
import tornadofx.setValue
import tornadofx.textfield
import tornadofx.toProperty
import tornadofx.treeview
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.collections.set

/**
 * @author Kevin Ludwig
 */
class MainView : View("%main"), CoroutineScope, SpecLookup {
    override val coroutineContext = Dispatchers.IO

    private val objectMapper by di<ObjectMapper>()
    private val injector by di<Injector>()
    private var graphProvider = injector.getProvider(VGraphDefault::class.java)

    private val hazelcastClient = HazelcastClient.newHazelcastClient(ClientConfig().apply {
        serializationConfig.apply {
            addSerializerConfig(SerializerConfig().setTypeClass(Spec.Node::class.java).setImplementation(object : StreamSerializer<Spec.Node> {
                override fun getTypeId() = 1

                override fun write(out: ObjectDataOutput, `object`: Spec.Node) {
                    objectMapper.writeValue(out, `object`)
                }

                override fun read(`in`: ObjectDataInput) = objectMapper.readValue(`in`, Spec.Node::class.java)
            }))
            addSerializerConfig(SerializerConfig().setTypeClass(VGraphDefault::class.java).setImplementation(object : StreamSerializer<VGraphDefault> {
                override fun getTypeId() = 2

                override fun write(out: ObjectDataOutput, `object`: VGraphDefault) {
                    objectMapper.writeValue(out, `object`)
                }

                override fun read(`in`: ObjectDataInput) = objectMapper.readValue(`in`, VGraphDefault::class.java).apply { specLookup = this@MainView }
            }))
        }
    })
    private val nodeSpecs = hazelcastClient.getMap<String, Spec.Node>("node_specs")
    private val graphs = hazelcastClient.getMap<String, VGraphDefault>("graphs")

    private val graphProperty = SimpleObjectProperty<VGraphDefault?>().apply { update { title = "Cran${it?.let { " - ${it.name}" } ?: "" }" } }
    private var graph by graphProperty

    override val root by fxml<Parent>("/main.fxml")
    private val rootHbox by fxid<HBox>()
    private lateinit var graphsTreeView: TreeView<Pair<String, VGraphDefault?>>
    private val graphScrollPane by fxid<ScrollPane>()
    private val graphPane by fxid<Pane>()
    private val jsonFormat by fxid<HBox>()
    private val jsonTextArea by fxid<TextArea>()

    init {
        rootHbox.children.add(0, drawer {
            item(messages["main.graphs"], null, true) {
                minWidth = 200.0
                maxWidth = 200.0

                graphsTreeView = treeview()
            }
        })

        with(graphsTreeView) {
            /*isEditable = true*/
            isShowRoot = false

            setCellFactory {
                TextFieldTreeCell<Pair<String, VGraphDefault?>>().apply {
                    setOnMouseClicked {
                        if (it.clickCount == 2) {
                            it.consume()
                            item?.second?.let { graph = it }
                        }
                    }

                    setOnDragDetected {
                        it.consume()
                        item?.second?.let {
                            startDragAndDrop(TransferMode.COPY).apply {
                                dragView = snapshot(null, null)
                                setContent { this[nodeSpecDataFormat] = objectMapper.writeValueAsString(it.toSpec()) }
                            }
                        }
                    }

                    converter = object : StringConverter<Pair<String, VGraphDefault?>>() {
                        override fun toString(`object`: Pair<String, VGraphDefault?>) = `object`.first

                        override fun fromString(string: String) = TODO()
                    }
                }
            }

            setOnKeyPressed {
                if (it.isControlDown) when (it.code) {
                    KeyCode.ENTER -> {
                        it.consume()
                        selectedValue?.second?.let { graph = it }
                    }
                    KeyCode.DELETE -> {
                        it.consume()
                        val children = mutableListOf(selectionModel.selectedItem)
                        while (children.isNotEmpty()) {
                            val child = children.removeLast()
                            child.value?.second?.let { graphs -= it.name }
                            children += child.children
                        }
                        selectionModel.selectedItem.removeFromParent()
                    }
                    else -> Unit
                }
            }

            contextmenu {
                item(messages["main.graphs.new"]) {
                    action {
                        val graph = graphProvider.get()
                        graphs[graph.name] = graph
                    }
                }
                separator()
                item(messages["main.graphs.delete"]) {
                    action {
                        val children = mutableListOf(selectionModel.selectedItem)
                        while (children.isNotEmpty()) {
                            val child = children.removeLast()
                            child.value?.second?.let { graphs -= it.name }
                            children += child.children
                        }
                        selectionModel.selectedItem.removeFromParent()
                    }
                }
                item(messages["main.graphs.rename"]) { action { selectionModel.selectedItem?.let { openInternalWindow(RenameView(it)) } } }
            }

            graphs.addEntryListener(object : EntryListener<String, VGraphDefault> {
                init {
                    clear()
                    graphs.values.forEach(::update)
                }

                private fun clear() {
                    root = TreeItem("" to null)
                    graph = null
                }

                private fun update(graph: VGraphDefault) {
                    val path = graph.name.split('/')
                    var current = root
                    path.forEachIndexed { i, _path -> current = current.children.find { it.value.first == _path }?.also { if (i == path.lastIndex) it.value = it.value.copy(second = graph) } ?: TreeItem(_path to if (i == path.lastIndex) graph else null).also { current.children += it } }
                    this@MainView.graph?.let { if (it.name == graph.name) this@MainView.graph = graph }
                }

                private fun remove(graphName: String) {
                    val pathIterator = graphName.split('/').iterator()
                    var current = root
                    var found = true
                    while (pathIterator.hasNext() && found) {
                        found = false

                        val path = pathIterator.next()
                        val childrenIterator = current.children.iterator()
                        while (childrenIterator.hasNext()) {
                            val child = childrenIterator.next()
                            if (child.value.first == path) {
                                if (!pathIterator.hasNext()) childrenIterator.remove()
                                else {
                                    current = child
                                    found = true
                                }
                                break
                            }
                        }
                    }
                    graph?.let { if (it.name == graphName) graph = null }
                }

                override fun entryAdded(event: EntryEvent<String, VGraphDefault>) = runLater { update(event.value) }

                override fun entryUpdated(event: EntryEvent<String, VGraphDefault>) = runLater { update(event.value) }

                override fun entryRemoved(event: EntryEvent<String, VGraphDefault>) = runLater { remove(event.key) }

                override fun entryEvicted(event: EntryEvent<String, VGraphDefault>) = runLater { remove(event.key) }

                override fun entryExpired(event: EntryEvent<String, VGraphDefault>) = runLater { remove(event.key) }

                override fun mapCleared(event: MapEvent) = runLater { clear() }

                override fun mapEvicted(event: MapEvent) = runLater { clear() }
            }, true)
        }

        // Graph
        with(graphScrollPane) {
            contextmenu {
                val searchProperty = "".toProperty()

                customitem(hideOnClick = false) {
                    textfield { textProperty().onChange { searchProperty.value = it } }.also {
                        setOnKeyPressed { _ ->
                            it.requestFocus()
                            it.positionCaret(it.length)
                        }
                    }
                }

                val nodeItems = mutableMapOf<String, MenuItem>()
                val treeItems = (PathTree(nodeSpecs.values) { it.name }.convert<MenuItem> { parent, tree ->
                    val _styleClass = tree.path.asStyleClass()
                    tree.value?.let {
                        MenuItem(tree.name).apply {
                            (parent as Menu?)?.let { it.items += this }
                            nodeItems[tree.path] = this

                            styleClass += _styleClass

                            action {
                                val inScreen = this@with.localToScreen(this@with.boundsInLocal)
                                graph?.newNode(it, Meta.Node(x - inScreen.minX, y - inScreen.minY))

                                // Hide on click
                                hide()
                            }
                        }
                    } ?: Menu(tree.name).apply {
                        (parent as Menu?)?.let { it.items += this }

                        styleClass += _styleClass
                    }
                } as Menu).items

                val items = items.toMutableList()
                this.items.addAll(treeItems)
                searchProperty.onChange { _search ->
                    this.items.setAll(items)
                    this.items.addAll(if (_search!!.isEmpty()) treeItems else nodeItems.filterKeys { it.contains(_search, true) }.values)
                }
            }
        }
        with(graphPane) {
            minWidthProperty().bind(graphScrollPane.widthProperty())
            minHeightProperty().bind(graphScrollPane.heightProperty())

            dynamicContent(graphProperty) { it?.flow?.setSkinFactories(SkinFactory(this, null, this@MainView)) }

            MouseControlUtil.addSelectionRectangleGesture(this, rectangle {
                stroke = Color.rgb(255, 255, 255, 1.0)
                fill = Color.rgb(0, 0, 0, 0.5)
            })

            setOnDragOver {
                if (it.dragboard.hasContent(nodeSpecDataFormat)) {
                    it.consume()
                    it.acceptTransferModes(TransferMode.COPY)
                }
            }

            setOnDragDropped {
                if (it.dragboard.hasContent(nodeSpecDataFormat)) {
                    it.consume()
                    graph?.newNode(objectMapper.readValue(it.dragboard.getContent(nodeSpecDataFormat) as String), Meta.Node(it.x, it.y))
                }
            }
        }

        // Json
        with(jsonTextArea) { graphProperty.onChange { text = it?.let { objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(it) } ?: "" } }
    }

    fun keyPressed(event: KeyEvent) {
        if (event.isControlDown) when (event.code) {
            KeyCode.A -> {
                event.consume()
                graph?.let { it.flow.nodes.forEach { it.requestSelection(true) } }
            }
            KeyCode.C -> {
                event.consume()
                graph?.let { Clipboard.getSystemClipboard().setContent { put(graphDataFormat, objectMapper.writeValueAsString(VGraphBase("", it.flow.nodes.filter(VNode::isSelected), emptyMap()).also { injector.injectMembers(it) })) } }
            }
            KeyCode.S -> {
                event.consume()
                graph?.let { graphs[it.name] = it }
            }
            KeyCode.V -> {
                event.consume()
                graph?.let { graph ->
                    Clipboard.getSystemClipboard().getContent(graphDataFormat)?.let {
                        graph.flow.nodes.filter(VNode::isSelected).forEach { it.requestSelection(false) }
                        graph.merge(objectMapper.readValue<GraphWithMetaBase>(it as String)).forEach {
                            it.x += 5.0
                            it.y += 5.0
                            it.requestSelection(true)
                        }
                    }
                }
            }
            KeyCode.X -> {
                event.consume()
                graph?.let { Clipboard.getSystemClipboard().setContent { put(graphDataFormat, objectMapper.writeValueAsString(VGraphBase("", it.flow.nodes.filter(VNode::isSelected).onEach(it.flow::remove), emptyMap()).also { injector.injectMembers(it) })) } }
            }
            else -> Unit
        } else when (event.code) {
            KeyCode.ENTER -> {
                event.consume()
                graph?.let { it.flow.nodes.filter(VNode::isSelected).forEach { openInternalWindow(PropertiesView(it)) } }
            }
            KeyCode.DELETE -> {
                event.consume()
                graph?.let { it.flow.nodes.filter(VNode::isSelected).forEach(it.flow::remove) }
            }
            KeyCode.F5 -> {
                event.consume()
                graphsTreeView.root = TreeItem("" to null)
                graph = null
                graphs.values.forEach { graph ->
                    val path = graph.name.split('/')
                    var current = graphsTreeView.root
                    path.forEachIndexed { i, _path -> current = current.children.find { it.value.first == _path }?.also { if (i == path.lastIndex) it.value = it.value.copy(second = graph) } ?: TreeItem(_path to if (i == path.lastIndex) graph else null).also { current.children += it } }
                    this@MainView.graph?.let { if (it.name == graph.name) this@MainView.graph = graph }
                }
            }
            KeyCode.F11 -> {
                event.consume()
                primaryStage.isFullScreen = !primaryStage.isFullScreen
            }
            else -> Unit
        }
    }

    fun fileSettingsMenuItemAction() {
        openInternalWindow<SettingsView>()
    }

    fun fileImportMenuItemAction() {
        chooseFile(filters = arrayOf(FileChooser.ExtensionFilter("Graph", "*.gph"), FileChooser.ExtensionFilter("All Files", "*.*"))).singleOrNull()?.let {
            val graph = it.inputStream().use { objectMapper.readValue<VGraphDefault>(GZIPInputStream(it)) }.apply { specLookup = this@MainView }
            graphs[graph.name] = graph
        }
    }

    fun fileExportAsMenuItemAction() {
        chooseFile(filters = arrayOf(FileChooser.ExtensionFilter("Graph", "*.gph"), FileChooser.ExtensionFilter("All Files", "*.*")), mode = FileChooserMode.Save).singleOrNull()?.let { it.outputStream().use { objectMapper.writeValue(GZIPOutputStream(it), graph) } }
    }

    fun fileExitMenuItemAction() {
        close()
    }

    fun editLayoutIsomMenuItemAction() {
        graph?.let {
            LayoutGeneratorSmart().apply {
                layoutSelector = 0
                workflow = it.flow.model
            }.generateLayout()
        }
    }

    fun editLayoutFrMenuItemAction() {
        graph?.let {
            LayoutGeneratorSmart().apply {
                layoutSelector = 1
                workflow = it.flow.model
            }.generateLayout()
        }
    }

    fun editLayoutKkMenuItemAction() {
        graph?.let {
            LayoutGeneratorSmart().apply {
                layoutSelector = 2
                workflow = it.flow.model
            }.generateLayout()
        }
    }

    fun editLayoutDagMenuItemAction() {
        graph?.let {
            LayoutGeneratorSmart().apply {
                layoutSelector = 3
                workflow = it.flow.model
            }.generateLayout()
        }
    }

    fun helpAboutMenuItemAction() {
        openInternalWindow<AboutView>()
    }

    fun jsonTabSelectionChanged() {
        jsonTextArea.text = graph?.let { objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(it) } ?: ""
    }

    override fun getNodeSpec(name: String) = nodeSpecs[name] ?: graphs[name]?.toSpec()

    companion object {
        private val graphDataFormat = DataFormat(Graph.MediaType)
        private val nodeSpecDataFormat = DataFormat(Spec.Node.MediaType)
    }
}
