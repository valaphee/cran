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

package com.valaphee.flow.spec

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.auto.service.AutoService
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.tools.StandardLocation

/**
 * @author Kevin Ludwig
 */
@AutoService(Processor::class)
class SpecGenerator : AbstractProcessor() {
    override fun getSupportedAnnotationTypes() = mutableSetOf(Node::class.java.name, In::class.java.name, Out::class.java.name)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnvironment: RoundEnvironment?): Boolean {
        if (annotations?.isEmpty() == true) return false
        roundEnvironment?.getElementsAnnotatedWith(Node::class.java)?.mapNotNull { `class` ->
            if (`class`.kind == ElementKind.CLASS && `class` is TypeElement) {
                if (!`class`.modifiers.contains(Modifier.ABSTRACT)) {
                    `class`.qualifiedName.toString() to Spec.Node(`class`.enclosedElements.mapNotNull { getter ->
                        if (getter.kind == ElementKind.METHOD && getter is ExecutableElement) {
                            val type = getter.returnType.toString()
                            val `in` = getter.getAnnotation(In::class.java)
                            val out = getter.getAnnotation(Out::class.java)
                            val json = getter.getAnnotation(JsonProperty::class.java)
                            if (`in` != null) if (out != null) error(`class`.qualifiedName) else {
                                Spec.Node.Port(`in`.value, when {
                                    type.contains("com.valaphee.flow.ControlPath") -> Spec.Node.Port.Type.InControl
                                    type.contains("com.valaphee.flow.DataPath") -> Spec.Node.Port.Type.InData
                                    else -> error(type)
                                }, json.value)
                            } else if (out != null) {
                                Spec.Node.Port(out.value, when {
                                    type.contains("com.valaphee.flow.ControlPath") -> Spec.Node.Port.Type.OutControl
                                    type.contains("com.valaphee.flow.DataPath") -> Spec.Node.Port.Type.OutData
                                    else -> error(type)
                                }, json.value)
                            } else null
                        } else null
                    })
                } else null
            } else null
        }?.toMap()?.let { processingEnv.filer.createResource(StandardLocation.CLASS_OUTPUT, "", "flow-spec.json").openOutputStream().use { stream ->  jacksonObjectMapper().writeValue(stream, it) } }
        return true
    }
}
