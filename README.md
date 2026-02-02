# Notifications Library

Librería de notificaciones en Java agnóstica a frameworks y extensible. Soporta múltiples canales (Email, SMS, Push) con diferentes proveedores, sistema de reintentos configurable, envío asíncrono y patrón Pub/Sub para eventos.

## Características

- ✅ **3 Canales**: Email, SMS, Push Notifications
- ✅ **6 Proveedores**: SendGrid, Mailgun, Twilio, AWS SNS, FCM, OneSignal
- ✅ **Sistema de Reintentos**: Configurable con backoff exponencial
- ✅ **Envío Asíncrono**: Soporte con CompletableFuture
- ✅ **Pub/Sub Pattern**: Eventos para seguimiento de notificaciones
- ✅ **Arquitectura Extensible**: Fácil agregar nuevos canales/proveedores
- ✅ **Principios SOLID**: Diseño limpio y mantenible
- ✅ **Tests Unitarios**: Cobertura completa con JUnit 5 y Mockito

## Requisitos

- Java 21 o superior
- Maven 3.6+

Para ejecutar solo los demos sin instalar Java ni Maven: **Docker** (ver sección [Docker](#docker)).

## Instalación

### Maven

Agrega la dependencia a tu `pom.xml`:

```xml
<dependency>
    <groupId>com.agora</groupId>
    <artifactId>notification-library</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Compilación local (desarrollo):**

```bash
mvn clean install
```

### Gradle

Agrega la dependencia en tu `build.gradle` (Groovy) o `build.gradle.kts` (Kotlin DSL):

**Groovy:**
```groovy
dependencies {
    implementation 'com.agora:notification-library:1.0.0'
}
```

**Kotlin DSL:**
```kotlin
dependencies {
    implementation("com.agora:notification-library:1.0.0")
}
```

**Compilación local:** Publica en el repositorio local con Maven (`mvn clean install`) y Gradle resolverá la dependencia desde allí, o configura un repositorio Maven local en tu `settings.gradle`.

## Quick Start

### Ejemplo básico (Email con SendGrid)

```java
var emailChannel = ChannelFactory.createChannel(
    Channel.EMAIL, "SendGrid",
    "SG.your-api-key", "sender@example.com", "Your App Name");

try (var service = NotificationServiceBuilder.builder()
        .registerChannel(emailChannel)
        .build()) {

    var request = NotificationRequest.builder()
        .channel(Channel.EMAIL)
        .recipient("user@example.com")
        .subject("Welcome!")
        .message("Hello World")
        .build();

    NotificationResult result = service.send(request);
    if (result.isSuccess()) {
        System.out.println("¡Enviado!");
    }
}
```

### Ejemplo asíncrono (SMS con Twilio)

```java
var smsChannel = ChannelFactory.createChannel(
    Channel.SMS, "Twilio", "AC123", "token", "+1234567890");

try (var service = NotificationServiceBuilder.builder()
        .registerChannel(smsChannel)
        .build()) {

    var request = NotificationRequest.builder()
        .channel(Channel.SMS)
        .recipient("+0987654321")
        .message("Hello")
        .build();

    CompletableFuture<NotificationResult> future = service.sendAsync(request);
    future.thenAccept(result -> {
        if (result.isSuccess()) System.out.println("Enviado!");
    }).exceptionally(e -> {
        System.out.println("Error: " + e.getMessage());
        return null;
    });
    future.join();
}
```

### Ejemplo con eventos (Pub/Sub)

```java
var pushChannel = ChannelFactory.createChannel(Channel.PUSH, "FCM", "your-fcm-key");

try (var service = NotificationServiceBuilder.builder()
        .registerChannel(pushChannel)
        .build()) {

    service.subscribe(event -> {
        System.out.println("Evento: " + event.getStatus() + " ID: " + event.getNotificationId());
    });

    service.send(request);
}
```

> **Importante:** `NotificationService` implementa `AutoCloseable`. Usa `try-with-resources` o llama a `service.shutdown()` al finalizar para liberar recursos (p. ej. ExecutorService en modo async).

## Configuración

Los canales se crean con **ChannelFactory.createChannel(tipo, proveedor, config...)** y se registran en el servicio con **NotificationServiceBuilder.registerChannel()**. El orden de `config` depende del proveedor.

### Email

| Proveedor | Llamada | Parámetros `config` |
|-----------|---------|---------------------|
| SendGrid  | `ChannelFactory.createChannel(Channel.EMAIL, "SendGrid", apiKey, fromEmail, fromName)` | apiKey, fromEmail, fromName |
| Mailgun   | `ChannelFactory.createChannel(Channel.EMAIL, "Mailgun", apiKey, fromEmail, fromName)` | apiKey, fromEmail, fromName |

**Ejemplo SendGrid:**

```java
var emailChannel = ChannelFactory.createChannel(
    Channel.EMAIL, "SendGrid",
    System.getenv("SENDGRID_API_KEY"),
    "sender@example.com",
    "Sender Name");

NotificationService service = NotificationServiceBuilder.builder()
    .registerChannel(emailChannel)
    .build();
```

**Ejemplo Mailgun:**

```java
var emailChannel = ChannelFactory.createChannel(
    Channel.EMAIL, "Mailgun",
    "key-your-mailgun-key", "sender@example.com", "Sender Name");

NotificationService service = NotificationServiceBuilder.builder()
    .registerChannel(emailChannel)
    .build();
```

### SMS

| Proveedor | Llamada | Parámetros `config` |
|-----------|---------|---------------------|
| Twilio    | `ChannelFactory.createChannel(Channel.SMS, "Twilio", accountSid, authToken, fromNumber)` | accountSid, authToken, fromNumber |
| AWS SNS   | `ChannelFactory.createChannel(Channel.SMS, "AwsSns", apiKey, apiSecret, region)` | apiKey, apiSecret, region |

**Ejemplo Twilio:**

```java
var smsChannel = ChannelFactory.createChannel(
    Channel.SMS, "Twilio",
    "AC1234567890", "auth-token-here", "+1234567890");

NotificationService service = NotificationServiceBuilder.builder()
    .registerChannel(smsChannel)
    .build();
```

**Ejemplo AWS SNS:**

```java
var smsChannel = ChannelFactory.createChannel(
    Channel.SMS, "AwsSns",
    "AKIAIOSFODNN7EXAMPLE", "wJalrXUtnFEMI/K7MDENG", "us-east-1");

NotificationService service = NotificationServiceBuilder.builder()
    .registerChannel(smsChannel)
    .build();
```

### Push

| Proveedor | Llamada | Parámetros `config` |
|-----------|---------|---------------------|
| FCM       | `ChannelFactory.createChannel(Channel.PUSH, "FCM", serverKey)` | serverKey |
| OneSignal | `ChannelFactory.createChannel(Channel.PUSH, "OneSignal", apiKey, appId)` | apiKey, appId |

**Ejemplo FCM:**

```java
var pushChannel = ChannelFactory.createChannel(Channel.PUSH, "FCM", "your-fcm-server-key");

NotificationService service = NotificationServiceBuilder.builder()
    .registerChannel(pushChannel)
    .build();
```

**Ejemplo OneSignal:**

```java
var pushChannel = ChannelFactory.createChannel(
    Channel.PUSH, "OneSignal", "api-key", "app-id");

NotificationService service = NotificationServiceBuilder.builder()
    .registerChannel(pushChannel)
    .build();
```

## Sistema de Reintentos

### Configuración de Reintentos

```java
RetryConfig retryConfig = RetryConfig.builder()
    .maxAttempts(3)
    .initialDelayMs(1000)
    .maxDelayMs(10000)
    .backoffMultiplier(2.0)
    .retryOnFailure(true)
    .build();

var emailChannel = ChannelFactory.createChannel(
    Channel.EMAIL, "SendGrid", "SG.your-api-key", "sender@example.com", "Sender Name");

NotificationService service = NotificationServiceBuilder.builder()
    .registerChannel(emailChannel)
    .retryConfig(retryConfig)
    .build();
```

### Backoff Exponencial

El sistema usa backoff exponencial:
- Intento 1: Sin delay
- Intento 2: `initialDelayMs` (ej: 1000ms)
- Intento 3: `initialDelayMs * multiplier` (ej: 2000ms)
- Intento 4: `initialDelayMs * multiplier^2` (ej: 4000ms)
- El delay se limita a `maxDelayMs`

## Proveedores Soportados

| Canal | Proveedor | API |
|-------|-----------|-----|
| Email | SendGrid | API v3 Mail Send |
| Email | Mailgun | Messages API |
| SMS | Twilio | Messages API |
| SMS | AWS SNS | Publish API |
| Push | Firebase Cloud Messaging (FCM) | v1 Send API |
| Push | OneSignal | Create Notification API |

## Guía de Extensión

### Agregar un nuevo proveedor

1. **Implementar el proveedor** (igual que antes: `NotificationProvider` + lógica de envío).

2. **Implementar `ChannelFactoryInterface`** y registrar en el registry:

```java
public class NewEmailChannelFactory implements ChannelFactoryInterface {
    @Override
    public NotificationChannel createChannel(String... config) {
        if (config == null || config.length < 3) return null;
        EmailConfig cfg = EmailConfig.builder()
            .apiKey(config[0]).fromEmail(config[1]).fromName(config[2])
            .apiUrl("https://api.newprovider.com").build();
        return new EmailChannel(new NewEmailProvider(cfg));
    }
    @Override
    public String getProviderName() { return "NewProvider"; }
    @Override
    public String getChannelType() { return Channel.EMAIL.name(); }
}

// Al arranque de la app (o en un módulo de configuración):
ChannelFactory.getRegistry().register(new NewEmailChannelFactory());
```

3. **Uso:** `ChannelFactory.createChannel(Channel.EMAIL, "NewProvider", apiKey, fromEmail, fromName)` y `NotificationServiceBuilder.builder().registerChannel(channel).build()`.

### Agregar un nuevo canal

1. Crear la clase del canal implementando `NotificationChannel` (y opcionalmente `RetryableChannel`).
2. Añadir el valor al enum `Channel` (EMAIL, SMS, PUSH, …).
3. Crear un `ChannelFactoryInterface` para ese canal y registrarlo en `ChannelFactory.getRegistry()`.
4. Usar `ChannelFactory.createChannel(Channel.NUEVO_CANAL, "NombreProveedor", ...)` y `registerChannel()`.

## API Reference

### Clases principales

| Clase | Uso |
|-------|-----|
| **ChannelFactory** | Crear canales: `createChannel(Channel tipo, String proveedor, String... config)`. Registry: `getRegistry().register(ChannelFactoryInterface)`. |
| **NotificationServiceBuilder** | Construir el servicio: `builder()`, `registerChannel(NotificationChannel)`, `retryConfig(RetryConfig)`, `executionMode(SYNC\|ASYNC)` o `sync()`/`async()`, `build()`. |
| **NotificationService** | Enviar y suscribirse: `send(request)`, `sendAsync(request)`, `subscribe(Consumer<NotificationEvent>)`, `shutdown()`/`close()`. |
| **NotificationRequest** | DTO de petición: `builder().channel(...).recipient(...).subject(...).message(...).title(...).body(...).build()`. |
| **NotificationResult** | Resultado: `isSuccess()`, `getStatus()`, `getProviderName()`, `getAttemptNumber()`, `getTimestamp()`, `getErrorDetails()`. |
| **NotificationEvent** | Eventos Pub/Sub: PENDING, RETRYING, SENT, FAILED. |

### NotificationServiceBuilder

- **`static builder()`** — Crea el builder.
- **`registerChannel(NotificationChannel channel)`** — Registra un canal (creado con `ChannelFactory.createChannel(...)`). Retorna `this`.
- **`retryConfig(RetryConfig config)`** — Aplica reintentos a canales que implementan `RetryableChannel`. Retorna `this`.
- **`executionMode(ExecutionMode mode)`** — SYNC (sin thread pool) o ASYNC (por defecto). Retorna `this`.
- **`sync()`** / **`async()`** — Atajos para el modo de ejecución. Retornan `this`.
- **`build()`** — Construye `NotificationService` (Sync o Async) con los canales registrados.

### ChannelFactory

- **`createChannel(Channel channelType, String providerName, String... config)`** — Crea un canal. El orden de `config` depende del proveedor (ver tablas en Configuración).
- **`getRegistry()`** — Devuelve el registry para registrar factories custom (`ChannelFactoryInterface`).

### NotificationService

#### `send(NotificationRequest request)`
Envía una notificación de forma síncrona.

**Parámetros:** `request` — Solicitud de notificación.  
**Retorna:** `NotificationResult`.

#### `sendAsync(NotificationRequest request)`
Envía una notificación de forma asíncrona.

**Parámetros:** `request` — Solicitud de notificación.  
**Retorna:** `CompletableFuture<NotificationResult>`.

#### `subscribe(Consumer<NotificationEvent> eventConsumer)`
Suscribe un consumidor a eventos de notificaciones.

**Parámetros:** `eventConsumer` — Consumidor de eventos.

### NotificationRequest

```java
NotificationRequest.builder()
    .channel(Channel.EMAIL)
    .recipient("user@example.com")
    .subject("Subject")           // Para email
    .message("Message body")
    .title("Title")              // Para push
    .body("Body")                // Para push
    .build();
```

### NotificationResult

```java
NotificationResult result = service.send(request);

result.isSuccess();              // boolean
result.getStatus();              // NotificationStatus
result.getProviderName();        // String
result.getAttemptNumber();       // int
result.getTimestamp();           // Instant
result.getErrorDetails();        // String
```

### NotificationEvent

Eventos publicados durante el ciclo de vida de la notificación:

- `PENDING`: Notificación encolada
- `RETRYING`: Notificación siendo reintentada
- `SENT`: Notificación enviada exitosamente
- `FAILED`: Notificación falló después de todos los reintentos

### Gestión de Recursos

`NotificationService` implementa `AutoCloseable`:

#### `shutdown()`
Cierra el ExecutorService y libera recursos. Seguro llamar múltiples veces.

#### `close()`
Equivalente a `shutdown()`. Se invoca automáticamente con `try-with-resources`.

```java
// Opción 1: try-with-resources (recomendado)
try (NotificationService service = NotificationServiceBuilder.builder()...build()) {
    service.send(request);
}

// Opción 2: shutdown explícito
NotificationService service = NotificationServiceBuilder.builder()...build();
try {
    service.send(request);
} finally {
    service.shutdown();
}
```

## Manejo de Errores

- **`send()`**: Lanza `RuntimeException` si falla. Captura con try-catch:

```java
try {
    NotificationResult result = service.send(request);
    if (!result.isSuccess()) {
        System.out.println("Error: " + result.getErrorDetails());
    }
} catch (Exception e) {
    System.err.println("Error enviando: " + e.getMessage());
}
```

- **`sendAsync()`**: Retorna `CompletableFuture`. Usa `exceptionally()` o `handle()` para errores:

```java
service.sendAsync(request)
    .thenAccept(result -> { /* éxito */ })
    .exceptionally(e -> {
        System.err.println("Error: " + e.getCause().getMessage());
        return null;
    });
```

- **`ProviderException`**, **`ValidationException`**: Excepciones específicas del dominio.

## Seguridad: Mejores prácticas para credenciales

1. **Nunca hardcodees credenciales**:
   ```java
   // ❌ MAL
   ChannelFactory.createChannel(Channel.EMAIL, "SendGrid", "hardcoded-key", ...);

   // ✅ BIEN
   String apiKey = System.getenv("SENDGRID_API_KEY");
   ChannelFactory.createChannel(Channel.EMAIL, "SendGrid", apiKey, fromEmail, fromName);
   ```

2. **Usa variables de entorno**:
   ```bash
   export SENDGRID_API_KEY="SG.your-key"
   export TWILIO_AUTH_TOKEN="your-token"
   ```

3. **Valida credenciales antes de usar**:
   ```java
   String apiKey = System.getenv("SENDGRID_API_KEY");
   if (apiKey == null || apiKey.isBlank()) {
       throw new IllegalStateException("SENDGRID_API_KEY not configured");
   }
   var channel = ChannelFactory.createChannel(Channel.EMAIL, "SendGrid", apiKey, from, name);
   ```

4. **Usa secretos gestionados**:
   - AWS Secrets Manager
   - HashiCorp Vault
   - Kubernetes Secrets

5. **Rotación de credenciales**:
   - Implementa rotación periódica
   - Usa múltiples credenciales para alta disponibilidad

## Ejecutar Ejemplos

El build genera un fat JAR (maven-shade-plugin) con `Main-Class: com.agora.notification.examples.NotificationExamples`.

```bash
mvn clean package -DskipTests

# Opción 1: ejecutar el JAR (recomendado)
java -jar target/notification-library-1.0.0.jar

# Opción 2: con Maven
mvn exec:java -Dexec.mainClass="com.agora.notification.examples.NotificationExamples" -DskipTests
```

## Docker

El **`Dockerfile`** en la raíz del proyecto define la imagen; los mismos comandos están en la cabecera del Dockerfile. Esta sección resume el uso y cómo llevar la imagen a otra máquina.

Multi-stage build: stage 1 compila con Maven (JDK 21) y genera el fat JAR; stage 2 expone solo el JAR sobre JRE 21. No requiere Java ni Maven en el host.

**Prerequisito:** Docker instalado y daemon en ejecución.

**Desde la raíz del proyecto** (donde está el `Dockerfile`):

```bash
docker build -t notification-library .
docker run --rm notification-library
```

El JAR incluye dependencias (shade) y `Main-Class`; el `CMD` ejecuta los ejemplos. Para ejecutar en otra máquina sin clonar: `docker save -o notification-library.tar notification-library:latest`, copiar el `.tar` y en la otra PC `docker load -i notification-library.tar` y `docker run --rm notification-library`; o subir la imagen a un registry (p. ej. Docker Hub) y hacer `docker run` desde allí.

## Testing

Tests unitarios con JUnit 5 + Mockito, usando mocks y simulaciones (sin conexiones HTTP reales).

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar test específico
mvn test -Dtest=EmailValidatorTest
```

## Estructura del Proyecto

```
notification-library/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/agora/notification/
│   │           ├── channels/          # Canales (Email, SMS, Push)
│   │           ├── config/            # Configuraciones
│   │           ├── core/              # Interfaces core
│   │           ├── events/            # Pub/Sub
│   │           ├── exceptions/        # Excepciones custom
│   │           ├── factory/           # Factory pattern
│   │           ├── models/            # DTOs y modelos
│   │           ├── providers/         # Proveedores
│   │           ├── retry/             # Sistema de reintentos
│   │           ├── service/           # Servicios
│   │           └── validation/        # Validadores
│   ├── test/
│   │   └── java/                      # Tests unitarios
│   └── examples/
│       └── java/                      # Ejemplos de uso
├── pom.xml
├── Dockerfile
└── README.md
```

## Contribuir

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## Licencia

Este proyecto es parte de un challenge técnico.

---

## Uso de IA

> *Completa esta sección si utilizaste herramientas de IA durante el desarrollo (GitHub Copilot, ChatGPT, Claude, Cursor, etc.):*
>
> - **Herramienta/modelo utilizado:** Cursor AI, Claude.
> - **Proceso de trabajo:** Desarrollo iterativo en 7 fases con revisión manual entre cada paso. Generación de código supervisada: interfaces core → canales → providers → async/retry → tests → documentación. Cada componente fue revisado y ajustado antes de continuar.
> - **Prompts o estrategias:** Referencias contextuales con @archivo para coherencia arquitectónica. Prompts estructurados en .cursorrules con reglas de patrones (Strategy, Factory, etc.), principios SOLID con ejemplos, y estándares de código (Lombok, inmutabilidad). Validación contra requirements del challenge en cada iteración. Ejemplo de prompt: "Implementar EmailChannel siguiendo Strategy pattern. Contexto: @requirements.txt, sin Spring, configuración por código Java puro. Validar Open/Closed Principle"
> - **Decisiones propias vs sugerencias de IA:** Propias: arquitectura, diseño de la librería, priorización de features (Async, Retry, Validaciones, Pub/Sub), trade-offs de simplicidad vs completitud. IA: Boilerplate (builders, DTOs, Lombok), implementación de providers basada en docs reales de APIs, estructura de tests con Mockito, README inicial y Dockerfile multi-stage, validaciones con regex.
> - **En qué ayudó y en qué no:** Ayudó en documentación clara, cobertura de tests, Dockerfile multi-stage y validación contra requirements. No sustituye decisiones decisiones arquitectónicas y evaluación de trade-offs, interpretación del contexto del challenge (qué es suficiente vs overkill), diseño de flujos de integración complejos, identificación de edge cases específicos del dominio.
