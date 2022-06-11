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
    implementation(project(":cran-meta"))
    implementation(project(":cran-spec"))

    implementation(project(":cran-pkg-base"))
    implementation(project(":cran-pkg-base-virtual"))
    implementation(project(":cran-pkg-input"))
    implementation(project(":cran-pkg-input-virtual"))

    implementation(libs.classgraph)
    implementation(libs.fasterxml.guice)
    implementation(libs.fasterxml.kotlin)
    implementation(libs.guice)
    implementation(libs.log4j.core)
    implementation(libs.log4j.iostreams)
    implementation(libs.log4j.jul)
    implementation(libs.log4j.slf4j.impl)
    implementation(libs.hazelcast)
}

tasks {
    build { dependsOn(shadowJar) }

    jar { manifest { attributes(mapOf("Main-Class" to "com.valaphee.cran.MainKt")) } }

    shadowJar { archiveName = "cran-env.jar" }
}
