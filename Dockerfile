# syntax=docker/dockerfile:1.7
FROM gradle:8.11.1-jdk17 AS builder
WORKDIR /workspace

COPY gradle gradle
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY common common
COPY services services
COPY web web

ARG GRADLE_TASK
ARG MODULE_DIR
RUN gradle "${GRADLE_TASK}:bootJar" --no-daemon -x test \
    && set -- "${MODULE_DIR}"/build/libs/*.jar \
    && executable_jar='' \
    && for jar in "$@"; do \
         case "$jar" in *-plain.jar) continue ;; esac; \
         test -z "$executable_jar" || { echo "Multiple executable JAR candidates found" >&2; exit 1; }; \
         executable_jar="$jar"; \
       done \
    && test -n "$executable_jar" \
    && cp "$executable_jar" /workspace/app.jar \
    && jar tf /workspace/app.jar | grep -q '^org/springframework/boot/loader/launch/JarLauncher.class$'

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN groupadd --system spring && useradd --system --gid spring spring
COPY --from=builder /workspace/app.jar /app/app.jar
USER spring:spring
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
