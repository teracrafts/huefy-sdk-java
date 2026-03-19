# huefy-java

Official Java SDK for [Huefy](https://huefy.dev) — transactional email delivery made simple.

## Installation

### Maven

```xml
<dependency>
    <groupId>com.huefy</groupId>
    <artifactId>huefy-java</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.huefy:huefy-java:1.0.0'
```

## Requirements

- Java 11+

## Quick Start

```java
import com.huefy.HuefyEmailClient;
import com.huefy.model.Recipient;
import com.huefy.model.SendEmailRequest;
import com.huefy.model.SendEmailResponse;
import java.util.Map;

HuefyEmailClient client = HuefyEmailClient.builder("sdk_your_api_key").build();

SendEmailRequest request = SendEmailRequest.builder()
    .templateKey("welcome-email")
    .recipient(new Recipient("alice@example.com", "Alice"))
    .variables(Map.of("firstName", "Alice", "trialDays", 14))
    .build();

SendEmailResponse response = client.sendEmail(request).get();
System.out.println("Message ID: " + response.getMessageId());
client.close();
```

## Key Features

- **Async API** — all methods return `CompletableFuture<T>` for non-blocking use
- **Synchronous convenience** — call `.get()` or `.join()` for blocking variants
- **Builder pattern** — all request and config objects use fluent builders
- **Retry with exponential backoff** — configurable attempts, base delay, ceiling, and jitter
- **Circuit breaker** — opens after 5 consecutive failures, probes after 30 s
- **HMAC-SHA256 signing** — optional request signing for additional integrity verification
- **Key rotation** — primary + secondary API key with seamless failover
- **Rate limit callbacks** — `onRateLimitUpdate` fires whenever rate-limit headers change
- **Thread-safe** — safe for concurrent use; implements `AutoCloseable`
- **PII detection** — warns when template variables contain sensitive field patterns

## Configuration Reference

| Builder Method | Default | Description |
|----------------|---------|-------------|
| `baseUrl(url)` | `https://api.huefy.dev/api/v1/sdk` | Override the API base URL |
| `timeout(ms)` | `30000` | Request timeout in milliseconds |
| `retryConfig(cfg)` | see below | Retry behaviour |
| `circuitBreakerConfig(cfg)` | see below | Circuit breaker behaviour |
| `logger(l)` | `ConsoleLogger` | Custom logging sink |
| `secondaryApiKey(key)` | — | Backup key used during key rotation |
| `enableRequestSigning(true)` | `false` | Enable HMAC-SHA256 request signing |
| `onRateLimitUpdate(fn)` | — | Callback fired on rate-limit header changes |

### RetryConfig defaults

| Field | Default | Description |
|-------|---------|-------------|
| `maxAttempts` | `3` | Total attempts including the first |
| `baseDelayMs` | `500` | Exponential backoff base delay (ms) |
| `maxDelayMs` | `10000` | Maximum backoff delay (ms) |
| `jitter` | `0.2` | Random jitter factor (0–1) |

### CircuitBreakerConfig defaults

| Field | Default | Description |
|-------|---------|-------------|
| `failureThreshold` | `5` | Consecutive failures before circuit opens |
| `resetTimeoutMs` | `30000` | Milliseconds before half-open probe |

## Bulk Email

```java
import com.huefy.model.BulkEmailRequest;
import com.huefy.model.BulkEmailResponse;
import java.util.List;

BulkEmailRequest bulk = BulkEmailRequest.builder()
    .emails(List.of(
        SendEmailRequest.builder()
            .templateKey("promo")
            .recipient(new Recipient("bob@example.com"))
            .build(),
        SendEmailRequest.builder()
            .templateKey("promo")
            .recipient(new Recipient("carol@example.com"))
            .build()
    ))
    .build();

BulkEmailResponse result = client.sendBulkEmails(bulk).get();
System.out.printf("Sent: %d, Failed: %d%n", result.getTotalSent(), result.getTotalFailed());
```

## Error Handling

```java
import com.huefy.exception.HuefyAuthException;
import com.huefy.exception.HuefyRateLimitException;
import com.huefy.exception.HuefyCircuitOpenException;
import java.util.concurrent.ExecutionException;

try {
    SendEmailResponse response = client.sendEmail(request).get();
    System.out.println("Delivered: " + response.getMessageId());
} catch (ExecutionException e) {
    Throwable cause = e.getCause();
    if (cause instanceof HuefyAuthException) {
        System.err.println("Invalid API key");
    } else if (cause instanceof HuefyRateLimitException rle) {
        System.err.printf("Rate limited. Retry after %ds%n", rle.getRetryAfter());
    } else if (cause instanceof HuefyCircuitOpenException) {
        System.err.println("Circuit open — service unavailable, backing off");
    } else {
        throw e;
    }
}
```

### Error Code Reference

| Exception | Code | Meaning |
|-----------|------|---------|
| `HuefyInitException` | 1001 | Client failed to initialise |
| `HuefyAuthException` | 1102 | API key rejected |
| `HuefyNetworkException` | 1201 | Upstream request failed |
| `HuefyCircuitOpenException` | 1301 | Circuit breaker tripped |
| `HuefyRateLimitException` | 2003 | Rate limit exceeded |
| `HuefyTemplateMissingException` | 2005 | Template key not found |

## Health Check

```java
HealthResponse health = client.healthCheck().get();
if (!"healthy".equals(health.getStatus())) {
    System.err.println("Huefy degraded: " + health.getStatus());
}
```

## Local Development

Set `HUEFY_MODE=local` to point the SDK at a local Huefy server, or override the base URL via the builder:

```java
HuefyEmailClient client = HuefyEmailClient.builder("sdk_local_key")
    .baseUrl("http://localhost:3000/api/v1/sdk")
    .build();
```

## Developer Guide

Full documentation, advanced patterns, and provider configuration are in the [Java Developer Guide](../../docs/spec/guides/java.guide.md).

## License

MIT
