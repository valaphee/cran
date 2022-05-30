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
    implementation(project(":cran"))
    implementation(project(":cran-ext-audio"))
    implementation(project(":cran-ext-input"))
    implementation(project(":cran-ext-network"))
    implementation(project(":cran-ext-network-http"))
    implementation(project(":cran-ext-radio"))
    implementation(project(":cran-meta"))
    implementation(project(":cran-spec"))
    implementation(project(":cran-svc"))

    implementation("com.fasterxml.jackson.module:jackson-module-guice:2.13.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.3")
    implementation("com.google.inject:guice:5.1.0")
    implementation("io.github.classgraph:classgraph:4.8.146")
    implementation("io.netty:netty-tcnative:2.0.52.Final")
    implementation("io.netty:netty-tcnative-boringssl-static:2.0.52.Final")
    implementation("io.netty:netty-tcnative-boringssl-static:2.0.52.Final:windows-x86_64")
    implementation("org.apache.logging.log4j:log4j-core:2.17.2")
    implementation("org.apache.logging.log4j:log4j-iostreams:2.17.2")
    implementation("org.apache.logging.log4j:log4j-jul:2.17.2")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.17.2")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.4")
}

tasks {
    jar { manifest { attributes(mapOf("Main-Class" to "com.valaphee.cran.MainKt")) } }

    shadowJar { archiveName = "cran-env.jar" }
}
