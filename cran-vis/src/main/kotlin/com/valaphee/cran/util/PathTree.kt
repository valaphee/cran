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

package com.valaphee.cran.util

import javafx.scene.control.TreeItem

/**
 * @author Kevin Ludwig
 */
class PathTree<T> constructor(
    val path: String,
    val name: String? = null,
    val value: T? = null
) {
    internal val children = mutableMapOf<String, PathTree<T>>()

    constructor() : this("")

    constructor(list: Collection<T>, getPath: (T) -> String) : this("") {
        list.forEach {
            val pathSplit = getPath(it).split('/')
            val path = StringBuilder()
            var child = this
            pathSplit.forEachIndexed { i, name ->
                if (path.isNotEmpty()) path.append('/')
                path.append(name)
                child = child.children.getOrPut(name) { PathTree(path.toString(), name, if (i == pathSplit.lastIndex) it else null) }
            }
        }
    }

    operator fun get(path: String): T? {
        val pathSplit = path.split('/')
        var child: PathTree<T>? = this
        pathSplit.forEach { child = child?.children?.get(it) }
        return child?.value
    }

    fun <U> convert(converter: (parent: U?, tree: PathTree<T>) -> U): U {
        val parent = converter(null, this)

        fun PathTree<T>.traverse(parent: U?) {
            children.values.sortedBy { it.name }.forEach { it.traverse(converter(parent, it)) }
        }

        traverse(parent)

        return parent
    }

    fun <U> convert(parent: U, converter: (parent: U?, tree: PathTree<T>) -> U, finalizer: (parent: U?, children: List<U>) -> Unit): U {
        fun PathTree<T>.traverse(parent: U?) {
            finalizer(parent, children.values.sortedBy { it.name }.map { converter(parent, it).also { child -> it.traverse(child) } })
        }

        traverse(parent)

        return parent
    }

    companion object {
        /**
         * Uses the tree item structure instead of the path tree structure, which might be incomplete
         * as this PathTree is currently only used for conversion with merging, and therefore the structure itself
         * is not updated, because of redundancy and complexity
         */
        operator fun <T> TreeItem<PathTree<T>>.get(path: String): T? {
            val pathSplit = path.split('/')
            var child: TreeItem<PathTree<T>>? = this
            pathSplit.forEach { name -> child = child?.children?.find { it.value.name == name } }
            return child?.value?.value
        }
    }
}
