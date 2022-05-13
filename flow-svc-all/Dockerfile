# syntax=docker/dockerfile:1
FROM openjdk:17
COPY build/libs/flow.jar /opt
WORKDIR /opt
ENTRYPOINT [ "java", "-jar", "/opt/flow.jar" ]
