# Notification Library

Java notification library, framework-agnostic and extensible. Supports multiple channels (Email, SMS, Push) with different providers, configurable retry system, asynchronous sending, and Pub/Sub pattern for events.

## Requirements

- Java 21 or higher
- Maven 3.6+

To run the demos only without installing Java or Maven: **Docker** (see [Docker](#docker) section).

## Installation

### Maven

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.agora</groupId>
    <artifactId>notification-library</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Local build (development):**

```bash
mvn clean install
```

### Gradle

Add the dependency in your `build.gradle` (Groovy) or `build.gradle.kts` (Kotlin DSL):

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

**Local build:** Publish to the local repository with Maven (`mvn clean install`) and Gradle will resolve the dependency from there, or configure a local Maven repository in your `settings.gradle`.

## Quick Start

### Basic example (Email with SendGrid)

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
        System.out.println("Sent!");
    }
}
```

### Asynchronous example (SMS with Twilio)

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
        if (result.isSuccess()) System.out.println("Sent!");
    }).exceptionally(e -> {
        System.out.println("Error: " + e.getMessage());
        return null;
    });
    future.join();
}
```

### Example with events (Pub/Sub)

```java
var pushChannel = ChannelFactory.createChannel(Channel.PUSH, "FCM", "your-fcm-key");

try (var service = NotificationServiceBuilder.builder()
        .registerChannel(pushChannel)
        .build()) {

    service.subscribe(event -> {
        System.out.println("Event: " + event.getStatus() + " ID: " + event.getNotificationId());
    });

    service.send(request);
}
```

> **Important:** `NotificationService` implements `AutoCloseable`. Use `try-with-resources` or call `service.shutdown()` when done to release resources (e.g. ExecutorService in async mode).

## Configuration

Channels are created with **ChannelFactory.createChannel(type, provider, config...)** and registered with the service via **NotificationServiceBuilder.registerChannel()**. The order of `config` depends on the provider.

### Email

| Provider | Call | `config` parameters |
|----------|------|---------------------|
| SendGrid  | `ChannelFactory.createChannel(Channel.EMAIL, "SendGrid", apiKey, fromEmail, fromName)` | apiKey, fromEmail, fromName |
| Mailgun   | `ChannelFactory.createChannel(Channel.EMAIL, "Mailgun", apiKey, fromEmail, fromName)` | apiKey, fromEmail, fromName |

**SendGrid example:**

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

**Mailgun example:**

```java
var emailChannel = ChannelFactory.createChannel(
    Channel.EMAIL, "Mailgun",
    "key-your-mailgun-key", "sender@example.com", "Sender Name");

NotificationService service = NotificationServiceBuilder.builder()
    .registerChannel(emailChannel)
    .build();
```

### SMS

| Provider | Call | `config` parameters |
|----------|------|---------------------|
| Twilio    | `ChannelFactory.createChannel(Channel.SMS, "Twilio", accountSid, authToken, fromNumber)` | accountSid, authToken, fromNumber |
| AWS SNS   | `ChannelFactory.createChannel(Channel.SMS, "AwsSns", apiKey, apiSecret, region)` | apiKey, apiSecret, region |

**Twilio example:**

```java
var smsChannel = ChannelFactory.createChannel(
    Channel.SMS, "Twilio",
    "AC1234567890", "auth-token-here", "+1234567890");

NotificationService service = NotificationServiceBuilder.builder()
    .registerChannel(smsChannel)
    .build();
```

**AWS SNS example:**

```java
var smsChannel = ChannelFactory.createChannel(
    Channel.SMS, "AwsSns",
    "AKIAIOSFODNN7EXAMPLE", "wJalrXUtnFEMI/K7MDENG", "us-east-1");

NotificationService service = NotificationServiceBuilder.builder()
    .registerChannel(smsChannel)
    .build();
```

### Push

| Provider | Call | `config` parameters |
|----------|------|---------------------|
| FCM       | `ChannelFactory.createChannel(Channel.PUSH, "FCM", serverKey)` | serverKey |
| OneSignal | `ChannelFactory.createChannel(Channel.PUSH, "OneSignal", apiKey, appId)` | apiKey, appId |

**FCM example:**

```java
var pushChannel = ChannelFactory.createChannel(Channel.PUSH, "FCM", "your-fcm-server-key");

NotificationService service = NotificationServiceBuilder.builder()
    .registerChannel(pushChannel)
    .build();
```

**OneSignal example:**

```java
var pushChannel = ChannelFactory.createChannel(
    Channel.PUSH, "OneSignal", "api-key", "app-id");

NotificationService service = NotificationServiceBuilder.builder()
    .registerChannel(pushChannel)
    .build();
```

## Retry System

### Retry Configuration

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

### Exponential Backoff

The system uses exponential backoff:
- Attempt 1: No delay
- Attempt 2: `initialDelayMs` (e.g. 1000ms)
- Attempt 3: `initialDelayMs * multiplier` (e.g. 2000ms)
- Attempt 4: `initialDelayMs * multiplier^2` (e.g. 4000ms)
- Delay is capped at `maxDelayMs`

## Supported Providers

| Channel | Provider | API |
|---------|----------|-----|
| Email | SendGrid | API v3 Mail Send |
| Email | Mailgun | Messages API |
| SMS | Twilio | Messages API |
| SMS | AWS SNS | Publish API |
| Push | Firebase Cloud Messaging (FCM) | v1 Send API |
| Push | OneSignal | Create Notification API |

## Extension Guide

### Adding a new provider

1. **Implement the provider** (same as before: `NotificationProvider` + send logic).

2. **Implement `ChannelFactoryInterface`** and register it in the registry:

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

// At application startup (or in a configuration module):
ChannelFactory.getRegistry().register(new NewEmailChannelFactory());
```

3. **Usage:** `ChannelFactory.createChannel(Channel.EMAIL, "NewProvider", apiKey, fromEmail, fromName)` and `NotificationServiceBuilder.builder().registerChannel(channel).build()`.

### Adding a new channel

1. Create the channel class implementing `NotificationChannel` (and optionally `RetryableChannel`).
2. Add the value to the `Channel` enum (EMAIL, SMS, PUSH, …).
3. Create a `ChannelFactoryInterface` for that channel and register it in `ChannelFactory.getRegistry()`.
4. Use `ChannelFactory.createChannel(Channel.NEW_CHANNEL, "ProviderName", ...)` and `registerChannel()`.

## API Reference

### Main classes

| Class | Usage |
|-------|-------|
| **ChannelFactory** | Create channels: `createChannel(Channel type, String provider, String... config)`. Registry: `getRegistry().register(ChannelFactoryInterface)`. |
| **NotificationServiceBuilder** | Build the service: `builder()`, `registerChannel(NotificationChannel)`, `retryConfig(RetryConfig)`, `executionMode(SYNC\|ASYNC)` or `sync()`/`async()`, `build()`. |
| **NotificationService** | Send and subscribe: `send(request)`, `sendAsync(request)`, `subscribe(Consumer<NotificationEvent>)`, `shutdown()`/`close()`. |
| **NotificationRequest** | Request DTO: `builder().channel(...).recipient(...).subject(...).message(...).title(...).body(...).build()`. |
| **NotificationResult** | Result: `isSuccess()`, `getStatus()`, `getProviderName()`, `getAttemptNumber()`, `getTimestamp()`, `getErrorDetails()`. |
| **NotificationEvent** | Pub/Sub events: PENDING, RETRYING, SENT, FAILED. |

### NotificationServiceBuilder

- **`static builder()`** — Creates the builder.
- **`registerChannel(NotificationChannel channel)`** — Registers a channel (created with `ChannelFactory.createChannel(...)`). Returns `this`.
- **`retryConfig(RetryConfig config)`** — Applies retries to channels that implement `RetryableChannel`. Returns `this`.
- **`executionMode(ExecutionMode mode)`** — SYNC (no thread pool) or ASYNC (default). Returns `this`.
- **`sync()`** / **`async()`** — Shortcuts for execution mode. Return `this`.
- **`build()`** — Builds `NotificationService` (Sync or Async) with the registered channels.

### ChannelFactory

- **`createChannel(Channel channelType, String providerName, String... config)`** — Creates a channel. The order of `config` depends on the provider (see tables in Configuration).
- **`getRegistry()`** — Returns the registry to register custom factories (`ChannelFactoryInterface`).

### NotificationService

#### `send(NotificationRequest request)`
Sends a notification synchronously.

**Parameters:** `request` — Notification request.  
**Returns:** `NotificationResult`.

#### `sendAsync(NotificationRequest request)`
Sends a notification asynchronously.

**Parameters:** `request` — Notification request.  
**Returns:** `CompletableFuture<NotificationResult>`.

#### `subscribe(Consumer<NotificationEvent> eventConsumer)`
Subscribes a consumer to notification events.

**Parameters:** `eventConsumer` — Event consumer.

### NotificationRequest

```java
NotificationRequest.builder()
    .channel(Channel.EMAIL)
    .recipient("user@example.com")
    .subject("Subject")           // For email
    .message("Message body")
    .title("Title")              // For push
    .body("Body")                // For push
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

Events published during the notification lifecycle:

- `PENDING`: Notification queued
- `RETRYING`: Notification being retried
- `SENT`: Notification sent successfully
- `FAILED`: Notification failed after all retries

### Resource Management

`NotificationService` implements `AutoCloseable`:

#### `shutdown()`
Closes the ExecutorService and releases resources. Safe to call multiple times.

#### `close()`
Equivalent to `shutdown()`. Invoked automatically with `try-with-resources`.

```java
// Option 1: try-with-resources (recommended)
try (NotificationService service = NotificationServiceBuilder.builder()...build()) {
    service.send(request);
}

// Option 2: explicit shutdown
NotificationService service = NotificationServiceBuilder.builder()...build();
try {
    service.send(request);
} finally {
    service.shutdown();
}
```

## Error Handling

- **`send()`**: Throws `RuntimeException` on failure. Catch with try-catch:

```java
try {
    NotificationResult result = service.send(request);
    if (!result.isSuccess()) {
        System.out.println("Error: " + result.getErrorDetails());
    }
} catch (Exception e) {
    System.err.println("Error sending: " + e.getMessage());
}
```

- **`sendAsync()`**: Returns `CompletableFuture`. Use `exceptionally()` or `handle()` for errors:

```java
service.sendAsync(request)
    .thenAccept(result -> { /* success */ })
    .exceptionally(e -> {
        System.err.println("Error: " + e.getCause().getMessage());
        return null;
    });
```

- **`ProviderException`**, **`ValidationException`**: Domain-specific exceptions.

## Security: Best practices for credentials

1. **Never hardcode credentials**:
   ```java
   // ❌ BAD
   ChannelFactory.createChannel(Channel.EMAIL, "SendGrid", "hardcoded-key", ...);

   // ✅ GOOD
   String apiKey = System.getenv("SENDGRID_API_KEY");
   ChannelFactory.createChannel(Channel.EMAIL, "SendGrid", apiKey, fromEmail, fromName);
   ```

2. **Use environment variables**:
   ```bash
   export SENDGRID_API_KEY="SG.your-key"
   export TWILIO_AUTH_TOKEN="your-token"
   ```

3. **Validate credentials before use**:
   ```java
   String apiKey = System.getenv("SENDGRID_API_KEY");
   if (apiKey == null || apiKey.isBlank()) {
       throw new IllegalStateException("SENDGRID_API_KEY not configured");
   }
   var channel = ChannelFactory.createChannel(Channel.EMAIL, "SendGrid", apiKey, from, name);
   ```

4. **Use managed secrets**:
   - AWS Secrets Manager
   - HashiCorp Vault
   - Kubernetes Secrets

5. **Credential rotation**:
   - Implement periodic rotation
   - Use multiple credentials for high availability

## Running Examples

The build produces a fat JAR (maven-shade-plugin) with `Main-Class: com.agora.notification.examples.NotificationExamples`.

```bash
mvn clean package -DskipTests

# Option 1: run the JAR (recommended)
java -jar target/notification-library-1.0.0.jar

# Option 2: with Maven
mvn exec:java -Dexec.mainClass="com.agora.notification.examples.NotificationExamples" -DskipTests
```

## Docker

The **`Dockerfile`** in the project root defines the image; the same commands are in the Dockerfile header. This section summarizes usage and how to move the image to another machine.

Multi-stage build: stage 1 compiles with Maven (JDK 21) and produces the fat JAR; stage 2 exposes only the JAR on JRE 21. No Java or Maven required on the host.

**Prerequisite:** Docker installed and daemon running.

**From the project root** (where the `Dockerfile` is):

```bash
docker build -t notification-library .
docker run --rm notification-library
```

The JAR includes dependencies (shade) and `Main-Class`; the `CMD` runs the examples. To run on another machine without cloning: `docker save -o notification-library.tar notification-library:latest`, copy the `.tar` file and on the other machine run `docker load -i notification-library.tar` and `docker run --rm notification-library`; or push the image to a registry (e.g. Docker Hub) and run `docker run` from there.

## Testing

Unit tests with JUnit 5 + Mockito, using mocks and simulations (no real HTTP connections).

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=EmailValidatorTest
```

## Project Structure

```
notification-library/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/agora/notification/
│   │           ├── channels/          # Channels (Email, SMS, Push)
│   │           ├── config/            # Configuration
│   │           ├── core/              # Core interfaces
│   │           ├── events/            # Pub/Sub
│   │           ├── exceptions/        # Custom exceptions
│   │           ├── factory/           # Factory pattern
│   │           ├── models/            # DTOs and models
│   │           ├── providers/         # Providers
│   │           ├── retry/             # Retry system
│   │           ├── service/           # Services
│   │           └── validation/        # Validators
│   ├── test/
│   │   └── java/                      # Unit tests
│   └── examples/
│       └── java/                      # Usage examples
├── pom.xml
├── Dockerfile
└── README.md
```

## Contributing

1. Fork the project
2. Create a branch for your feature (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is part of a technical challenge.

---

## AI Usage

> *Complete this section if you used AI tools during development (GitHub Copilot, ChatGPT, Claude, Cursor, etc.):*
>
> - **Tool/model used:** Cursor AI, Claude.
> - **Workflow:** Iterative development in 7 phases with manual review between each step. Supervised code generation: core interfaces → channels → providers → async/retry → tests → documentation. Each component was reviewed and adjusted before continuing.
> - **Prompts or strategies:** Context references with @file for architectural consistency. Structured prompts in .cursorrules with pattern rules (Strategy, Factory, etc.), SOLID principles with examples, and code standards (Lombok, immutability). Validation against challenge requirements at each iteration. Example prompt: "Implement EmailChannel following Strategy pattern. Context: @requirements.txt, no Spring, pure Java code configuration. Validate Open/Closed Principle"
> - **Own decisions vs AI suggestions:** Own: architecture, library design, feature prioritization (Async, Retry, Validations, Pub/Sub), simplicity vs completeness trade-offs. AI: Boilerplate (builders, DTOs, Lombok), provider implementation based on real API docs, test structure with Mockito, initial README and multi-stage Dockerfile, regex validations.
> - **What helped and what didn't:** Helped with clear documentation, test coverage, multi-stage Dockerfile and validation against requirements. Does not replace architectural decisions and trade-off evaluation, interpretation of challenge context (what is enough vs overkill), design of complex integration flows, identification of domain-specific edge cases.
