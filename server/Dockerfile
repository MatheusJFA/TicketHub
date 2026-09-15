# syntax=docker/dockerfile:1
FROM maven:3.9.16-eclipse-temurin-25-alpine AS build
ARG SKIP_TESTS=true
WORKDIR /workspace

COPY pom.xml ./
COPY domain/pom.xml domain/pom.xml
COPY application/pom.xml application/pom.xml
COPY infrastructure/pom.xml infrastructure/pom.xml
COPY domain/src domain/src
COPY application/src application/src
COPY infrastructure/src infrastructure/src

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp -DskipTests=${SKIP_TESTS} package

FROM eclipse-temurin:25-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S tickethub && adduser -S -G tickethub tickethub
COPY --from=build --chown=tickethub:tickethub /workspace/infrastructure/target/infrastructure-0.0.1-SNAPSHOT.jar app.jar

USER tickethub
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
