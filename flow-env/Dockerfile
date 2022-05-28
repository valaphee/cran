# syntax=docker/dockerfile:1
FROM openjdk:17
COPY build/libs/flow-env.jar /opt
WORKDIR /opt
ENTRYPOINT [ "java", "-jar", "/opt/flow-env.jar" ]
