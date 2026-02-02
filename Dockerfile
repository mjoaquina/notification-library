################################################################################
# Notification Library - Multi-stage Build
# 
# Java 21 notification library with Email, SMS, and Push support.
# Optimized Docker image using multi-stage build (JDK → JRE Alpine).
#
# Quick Start:
#   docker build -t notification-library .
#   docker run --rm notification-library
#
################################################################################

FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
RUN apk add --no-cache maven

# Cache dependencies separately for faster rebuilds
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# --- Runtime Stage ---
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="tu-email@ejemplo.com" \
      version="1.0.0" \
      description="Multi-channel notification library for Java"

WORKDIR /app
COPY --from=build /app/target/notification-library-1.0.0.jar app.jar

ENV JAVA_OPTS=""
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
