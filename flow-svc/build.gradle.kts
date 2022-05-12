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

dependencies {
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-smile:2.13.2")
    api("io.grpc:grpc-netty:1.46.0")
    api("io.grpc:grpc-protobuf:1.46.0")
    api("io.grpc:grpc-stub:1.46.0")
    api("javax.annotation:javax.annotation-api:1.3.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
}

java.sourceSets.getByName("main").java.srcDir("build/generated/source/proto/main/java")
