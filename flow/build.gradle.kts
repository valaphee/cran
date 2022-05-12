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
    kotlin("kapt")
    `maven-publish`
    id("me.champeau.jmh")
}

dependencies {
    api(project(":flow-spec"))
    kapt(project(":flow-spec"))
    implementation("com.google.guava:guava:31.1-jre")
    api("com.valaphee:foundry-math:1.4.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.1-native-mt")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    jmh("org.openjdk.jmh:jmh-core:1.35")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.35")
    jmhAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.35")
}

java {
    withJavadocJar()
    withSourcesJar()
}

signing { sign(publishing.publications) }

publishing {
    publications {
        create<MavenPublication>("maven") {
            pom.apply {
                name.set("Flow")
                description.set("")
                url.set("https://valaphee.com")
                scm {
                    connection.set("https://github.com/valaphee/flow.git")
                    developerConnection.set("https://github.com/valaphee/flow.git")
                    url.set("https://github.com/valaphee/flow")
                }
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://raw.githubusercontent.com/valaphee/flow/master/LICENSE.txt")
                    }
                }
                developers {
                    developer {
                        id.set("valaphee")
                        name.set("Valaphee")
                        email.set("iam@valaphee.com")
                        roles.add("owner")
                    }
                }
            }

            from(components["java"])
        }
    }
}
