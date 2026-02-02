# Notification Library — multi-stage build
#
# Documentación: README, sección Docker (uso completo y ejecución en otra máquina).
#
# Prerequisito: Docker instalado y daemon en ejecución (p. ej. Docker Desktop abierto).
#
# Qué hace: Stage 1 compila con Maven (JDK 21) y genera un fat JAR (maven-shade-plugin)
# con Main-Class = NotificationExamples. Stage 2 es la imagen final: solo JRE 21 + ese JAR.
# No necesitas Java ni Maven en tu máquina; todo ocurre dentro del contenedor de build.
#
# Uso (desde la raíz del proyecto; el "." es la carpeta actual = contexto de build):
#   1. Construir la imagen:
#      docker build -t notification-library .
#   2. Ejecutar los demos (salida de NotificationExamples en consola):
#      docker run --rm notification-library
#
# -t notification-library  → nombre de la imagen.
# --rm                     → elimina el contenedor al terminar.

# --- Stage 1: build (no queda en la imagen final) ---
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

RUN apk add --no-cache maven

# Caché de capas: dependencias aparte; solo se recompila si cambian pom o src
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# --- Stage 2: runtime (imagen final: solo JRE + JAR) ---
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/notification-library-1.0.0.jar app.jar

ENV JAVA_OPTS=""
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
