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

/**
 * @author Kevin Ludwig
 */
class PathTree<T> private constructor(
    val path: String,
    val name: String? = null,
    val value: T? = null
) {
    private val children = mutableMapOf<String, PathTree<T>>()

    constructor(list: List<T>, getPath: (T) -> String) : this("") {
        list.forEach { value ->
            val pathSplit = getPath(value).split('/')
            val path = StringBuilder()
            var child = this
            pathSplit.forEachIndexed { i, name ->
                if (path.isNotEmpty()) path.append('/')
                path.append(name)
                child = child.children.getOrPut(name) { PathTree(path.toString(), name, if (i == pathSplit.lastIndex) value else null) }
            }
        }
    }

    fun <U> convert(converter: (U?, PathTree<T>) -> U): U {
        val parent = converter(null, this)

        fun PathTree<T>.traverse(parent: U?) {
            children.values.sortedBy { it.name }.forEach { it.traverse(converter(parent, it)) }
        }

        traverse(parent)

        return parent
    }
}
