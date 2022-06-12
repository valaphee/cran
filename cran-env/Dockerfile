# syntax=docker/dockerfile:1
FROM openjdk:17
COPY build/libs/flow-env.jar /opt
WORKDIR /opt
ENTRYPOINT [ "java", "--add-modules", "jdk.incubator.vector", "--add-modules", "java.se", "--add-exports", "java.base/jdk.internal.ref=ALL-UNNAMED", "--add-opens", "java.base/java.lang=ALL-UNNAMED", "--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED", "--add-opens", "java.management/sun.management=ALL-UNNAMED", "--add-opens", "jdk.management/com.sun.management.internal=ALL-UNNAMED", "-jar", "/opt/flow-env.jar" ]
