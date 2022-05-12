# syntax=docker/dockerfile:1
FROM openjdk:17
COPY build/libs/flow-svc-all.jar /opt
WORKDIR /opt
ENTRYPOINT [ "java", "-jar", "/opt/flow-svc-all.jar" ]
