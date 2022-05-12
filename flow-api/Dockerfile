# syntax=docker/dockerfile:1
FROM openjdk:17
COPY build/libs/flow-api.jar /opt
WORKDIR /opt
ENTRYPOINT [ "java", "-jar", "/opt/flow-api.jar" ]
