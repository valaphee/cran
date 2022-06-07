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

plugins { `maven-publish` }

dependencies {
    runtimeOnly("io.netty:netty-tcnative:2.0.52.Final")
    runtimeOnly("io.netty:netty-tcnative-boringssl-static:2.0.52.Final")
    runtimeOnly("io.netty:netty-tcnative-boringssl-static:2.0.52.Final:windows-x86_64")

    api(libs.grpc.netty)
    api(libs.grpc.protobuf)
    api(libs.grpc.stub)
    api("javax.annotation:javax.annotation-api:1.3.2")
}

java.sourceSets.getByName("main").java.srcDir("build/generated/source/proto/main/java")

java {
    withJavadocJar()
    withSourcesJar()
}

signing { sign(publishing.publications) }

publishing {
    publications {
        create<MavenPublication>("maven") {
            pom.apply {
                name.set("Cran Service")
                description.set("Flow-based programming \"language\"")
                url.set("https://valaphee.com")
                scm {
                    connection.set("https://github.com/valaphee/cran.git")
                    developerConnection.set("https://github.com/valaphee/cran.git")
                    url.set("https://github.com/valaphee/cran")
                }
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://raw.githubusercontent.com/valaphee/cran/master/LICENSE.txt")
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
