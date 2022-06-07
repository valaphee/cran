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

package com.valaphee.cran.spec.util

import java.nio.charset.StandardCharsets

fun String.asHexStringToByteArray() = ByteArray(length / 2) { (Character.digit(this[it * 2], 16) shl 4 or Character.digit(this[(it * 2) + 1], 16)).toByte() }

fun ByteArray.toHexString(): String {
    val bytes = ByteArray(size * 2)
    forEachIndexed { i, value ->
        val _value = value.toInt() and 0xFF
        bytes[i * 2] = hexDigits[_value ushr 4]
        bytes[i * 2 + 1] = hexDigits[_value and 0x0F]
    }
    return String(bytes, StandardCharsets.UTF_8)
}

private val hexDigits = "0123456789abcdef".toByteArray(StandardCharsets.US_ASCII)
