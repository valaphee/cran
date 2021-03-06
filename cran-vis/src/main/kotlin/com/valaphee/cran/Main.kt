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

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair
import com.fasterxml.jackson.module.guice.GuiceAnnotationIntrospector
import com.fasterxml.jackson.module.guice.GuiceInjectableValues
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Provides
import com.google.inject.Singleton
import com.valaphee.cran.settings.Settings
import tornadofx.DIContainer
import tornadofx.FX
import tornadofx.launch
import java.io.File
import kotlin.reflect.KClass
import kotlin.system.exitProcess

lateinit var injector: Injector

fun main(arguments: Array<String>) {
    injector = Guice.createInjector(object : AbstractModule() {
        @Provides
        @Singleton
        fun objectMapper(injector: Injector) = jacksonObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).apply {
            val guiceAnnotationIntrospector = GuiceAnnotationIntrospector()
            setAnnotationIntrospectors(AnnotationIntrospectorPair(guiceAnnotationIntrospector, serializationConfig.annotationIntrospector), AnnotationIntrospectorPair(guiceAnnotationIntrospector, deserializationConfig.annotationIntrospector))
            injectableValues = GuiceInjectableValues(injector)
        }

        @Provides
        @Singleton
        fun settings(objectMapper: ObjectMapper): Settings {
            val file = File(File(System.getProperty("user.home"), ".valaphee/cran").also(File::mkdirs), "settings.json")
            return if (!file.exists()) Settings().apply { objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, this) } else objectMapper.readValue(file)
        }
    })

    FX.dicontainer = object : DIContainer {
        override fun <T : Any> getInstance(type: KClass<T>) = injector.getInstance(type.java)
    }

    launch<MainApp>(arguments)

    exitProcess(0)
}
