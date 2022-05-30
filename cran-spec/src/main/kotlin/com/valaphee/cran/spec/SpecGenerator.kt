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

package com.valaphee.cran.spec

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.auto.service.AutoService
import com.valaphee.cran.spec.util.toHexString
import java.security.MessageDigest
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
    override fun getSupportedAnnotationTypes() = mutableSetOf(NodeType::class.java.name)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnvironment: RoundEnvironment?): Boolean {
        if (annotations?.isEmpty() == true) return false
        roundEnvironment?.getElementsAnnotatedWith(NodeType::class.java)?.mapNotNull { `class` ->
            if (`class`.kind == ElementKind.CLASS && `class` is TypeElement) {
                if (!`class`.modifiers.contains(Modifier.ABSTRACT)) {
                    Spec.Node(`class`.getAnnotation(NodeType::class.java).name, `class`.qualifiedName.toString(), `class`.enclosedElements.mapNotNull { getter ->
                        if (getter.kind == ElementKind.METHOD && getter is ExecutableElement) {
                            val `in` = getter.getAnnotation(In::class.java)
                            val out = getter.getAnnotation(Out::class.java)
                            val const = getter.getAnnotation(Const::class.java)
                            val json = getter.getAnnotation(JsonProperty::class.java)
                            if (`in` != null)
                                if (out != null) {
                                    processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Only @In or @Out is allowed, not both.")
                                    return true
                                } else if (const != null) {
                                    processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Only @In or @Const is allowed, not both.")
                                    return true
                                } else `in`.data.takeIf { it.isNotEmpty() }?.let { objectMapper.readTree(it) }?.let { Spec.Node.Port(`in`.name, json.value, Spec.Node.Port.Type.InData, it) } ?: Spec.Node.Port(`in`.name, json.value, Spec.Node.Port.Type.InControl, NullNode.instance)
                            else if (out != null)
                                if (const != null) {
                                    processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Only @Out or @Const is allowed, not both.")
                                    return true
                                } else out.data.takeIf { it.isNotEmpty() }?.let { objectMapper.readTree(it) }?.let { Spec.Node.Port(out.name, json.value, Spec.Node.Port.Type.OutData, it) } ?: Spec.Node.Port(out.name, json.value, Spec.Node.Port.Type.OutControl, NullNode.instance)
                            else if (const != null) const.data.takeIf { it.isNotEmpty() }?.let { objectMapper.readTree(it) }?.let { Spec.Node.Port(const.name, json.value, Spec.Node.Port.Type.Const, it) } ?: return true
                            else null
                        } else null
                    })
                } else null
            } else null
        }?.let {
            val bytes = objectMapper.writeValueAsBytes(Spec(it))
            processingEnv.filer.createResource(StandardLocation.CLASS_OUTPUT, "", "spec.${MessageDigest.getInstance("MD5").digest(bytes).toHexString()}.dat").openOutputStream().use { it.write(bytes) }
        }
        return true
    }

    companion object {
        private val objectMapper = jacksonObjectMapper()
    }
}
