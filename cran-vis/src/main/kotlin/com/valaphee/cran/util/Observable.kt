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

import javafx.beans.value.ObservableDoubleValue
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import tornadofx.onChange

fun ObservableDoubleValue.update(op: (Double) -> Unit) = apply {
    onChange { op(it) }
    op(value?.toDouble() ?: 0.0)
}

fun <T> ObservableValue<T>.update(op: (T?) -> Unit) = apply {
    onChange { op(it) }
    op(value)
}

fun <T> ObservableList<T>.update(op: (ObservableList<out T>) -> Unit) = apply {
    onChange { op(it.list) }
    op(this)
}
