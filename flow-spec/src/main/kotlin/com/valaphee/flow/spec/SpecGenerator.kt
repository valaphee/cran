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
import javax.tools.Diagnostic
import javax.tools.StandardLocation

/**
 * @author Kevin Ludwig
 */
@AutoService(Processor::class)
class SpecGenerator : AbstractProcessor() {
    override fun getSupportedAnnotationTypes() = mutableSetOf(Node::class.java.name)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnvironment: RoundEnvironment?): Boolean {
        if (annotations?.isEmpty() == true) return false
        roundEnvironment?.getElementsAnnotatedWith(Node::class.java)?.mapNotNull { `class` ->
            if (`class`.kind == ElementKind.CLASS && `class` is TypeElement) {
                if (!`class`.modifiers.contains(Modifier.ABSTRACT)) {
                    val node = `class`.getAnnotation(Node::class.java)
                    Spec.Node(node.value, `class`.enclosedElements.mapNotNull { getter ->
                        if (getter.kind == ElementKind.METHOD && getter is ExecutableElement) {
                            val type = getter.returnType.toString()
                            val `in` = getter.getAnnotation(In::class.java)
                            val out = getter.getAnnotation(Out::class.java)
                            val const = getter.getAnnotation(Const::class.java)
                            val json = getter.getAnnotation(JsonProperty::class.java)
                            if (`in` != null) if (out != null) {
                                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Only @In or @Out is allowed, not both.")
                                return true
                            } else if (const != null) {
                                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Only @In or @Const is allowed, not both.")
                                return true
                            } else {
                                Spec.Node.Port(
                                    `in`.value, when {
                                        type.contains("com.valaphee.flow.ControlPath") -> Spec.Node.Port.Type.InControl
                                        type.contains("com.valaphee.flow.DataPath") -> Spec.Node.Port.Type.InData
                                        else -> {
                                            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Unknown @In type $type.")
                                            return true
                                        }
                                    }, type.contains("Map"), false, json.value
                                )
                            } else if (out != null) if (const != null) {
                                processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Only @Out or @Const is allowed, not both.")
                                return true
                            } else {
                                Spec.Node.Port(
                                    out.value, when {
                                        type.contains("com.valaphee.flow.ControlPath") -> Spec.Node.Port.Type.OutControl
                                        type.contains("com.valaphee.flow.DataPath") -> Spec.Node.Port.Type.OutData
                                        else -> {
                                            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Unknown @Out type $type.")
                                            return true
                                        }
                                    }, type.contains("Map"), `out`.optional, json.value
                                )
                            } else if (const != null) Spec.Node.Port(const.value, Spec.Node.Port.Type.Const, false, false, json.value) else null
                        } else null
                    }, `class`.qualifiedName.toString())
                } else null
            } else null
        }?.let { processingEnv.filer.createResource(StandardLocation.CLASS_OUTPUT, "", "spec.json").openOutputStream().use { stream -> jacksonObjectMapper().writeValue(stream, Spec(it)) } }
        return true
    }
}
