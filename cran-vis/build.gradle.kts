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
    implementation(project(":cran-meta"))
    implementation(project(":cran-spec"))

    implementation(project(":cran-pkg-base"))

    implementation(libs.fasterxml.guice)
    implementation(libs.fasterxml.kotlin)
    implementation(libs.guice)
    implementation(libs.hazelcast)
    implementation(libs.tornadofx)
    implementation(libs.kotlinx.coroutines.javafx)
    implementation(libs.kotlinx.coroutines.jdk8)
    implementation(libs.vworkflowsfx)
}

tasks {
    build { dependsOn(shadowJar) }

    jar { manifest { attributes(mapOf("Main-Class" to "com.valaphee.cran.MainKt")) } }

    shadowJar { archiveName = "cran-vis.jar" }
}

javafx { modules("javafx.controls", "javafx.fxml", "javafx.graphics") }
