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

plugins {
    id("com.github.johnrengelman.shadow")
    id("org.openjfx.javafxplugin")
}

dependencies {
    implementation(project(":flow-meta"))
    implementation(project(":flow-spec"))
    implementation("de.codecentric.centerdevice:javafxsvg:1.3.0")
    implementation("io.ktor:ktor-client-content-negotiation:2.0.1")
    implementation("io.ktor:ktor-client-okhttp:2.0.1")
    implementation("io.ktor:ktor-serialization-jackson:2.0.1")
    implementation("no.tornado:tornadofx:1.7.20")
    implementation("org.controlsfx:controlsfx:11.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.6.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    implementation("eu.mihosoft.vrl.workflow:vworkflows-fx:0.2.5.0")
}

tasks {
    jar { manifest { attributes(mapOf("Main-Class" to "com.valaphee.flow.MainKt")) } }

    shadowJar { archiveName = "flow-gui.jar" }
}

javafx { modules("javafx.controls", "javafx.graphics") }