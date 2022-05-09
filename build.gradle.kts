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

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("com.palantir.git-version") version "0.12.3"
    kotlin("jvm") version "1.6.21"
    kotlin("kapt") version "1.6.21"
    `maven-publish`
    id("me.champeau.jmh") version "0.6.6"
    id("org.openjfx.javafxplugin") version "0.0.10"
    signing
}

allprojects { repositories { mavenCentral() } }

subprojects {
    apply(plugin = "com.palantir.git-version")
    apply(plugin = "kotlin")
    apply(plugin = "signing")

    group = "com.valaphee"
    val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
    val details = versionDetails()
    version = "${details.lastTag}.${details.commitDistance}"

    tasks {
        withType<JavaCompile> {
            sourceCompatibility = "16"
            targetCompatibility = "16"
        }

        withType<KotlinCompile>().configureEach { kotlinOptions { jvmTarget = "16" } }

        withType<Test> { useJUnitPlatform() }
    }

    signing { useGpgCmd() }
}
