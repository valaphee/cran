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

plugins { id("com.github.johnrengelman.shadow") }

dependencies {
    implementation(project(":flow"))
    implementation(project(":flow-ext-hid"))
    implementation(project(":flow-meta"))
    implementation(project(":flow-svc"))
    implementation("io.github.classgraph:classgraph:4.8.146")
    implementation("org.apache.logging.log4j:log4j-core:2.17.2")
    implementation("org.apache.logging.log4j:log4j-iostreams:2.17.2")
    implementation("org.apache.logging.log4j:log4j-jul:2.17.2")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.17.2")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.4")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
}

tasks {
    jar { manifest { attributes(mapOf("Main-Class" to "com.valaphee.flow.MainKt")) } }

    shadowJar { archiveName = "flow.jar" }
}
