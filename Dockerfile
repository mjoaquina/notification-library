# Notification Library — multi-stage build
#
# Documentation: README, Docker section (full usage and running on another machine).
#
# Prerequisite: Docker installed and daemon running (e.g. Docker Desktop open).
#
# What it does: Stage 1 compiles with Maven (JDK 21) and produces a fat JAR (maven-shade-plugin)
# with Main-Class = NotificationExamples. Stage 2 is the final image: JRE 21 only + that JAR.
# You don't need Java or Maven on your machine; everything runs inside the build container.
#
# Usage (from project root; "." is the current folder = build context):
#   1. Build the image:
#      docker build -t notification-library .
#   2. Run the demos (NotificationExamples output to console):
#      docker run --rm notification-library
#
# -t notification-library  → image name.
# --rm                     → remove the container when it exits.

# --- Stage 1: build (not included in final image) ---
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

RUN apk add --no-cache maven

# Layer cache: dependencies separate; only recompiles if pom or src change
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# --- Stage 2: runtime (final image: JRE + JAR only) ---
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/notification-library-1.0.0.jar app.jar

ENV JAVA_OPTS=""
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
