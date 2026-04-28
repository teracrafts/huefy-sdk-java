# huefy-sdk

Official Java SDK for [Huefy](https://huefy.dev) — transactional email delivery made simple.

## Installation

### Maven

```xml
<dependency>
    <groupId>com.teracrafts</groupId>
    <artifactId>huefy-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.teracrafts:huefy-sdk:1.0.0'
```

## Requirements

- Java 17+

## Quick Start

```java
import com.teracrafts.huefy.client.HuefyEmailClient;
import com.teracrafts.huefy.models.SendEmailRequest;
import com.teracrafts.huefy.models.SendEmailResponse;
import java.util.Map;

HuefyEmailClient client = new HuefyEmailClient("sdk_your_api_key");

SendEmailRequest request = new SendEmailRequest(
    "welcome-email",
    Map.of("firstName", "Alice", "trialDays", 14),
    "alice@example.com"
);

SendEmailResponse response = client.sendEmail(request);
System.out.println("Email ID: " + response.data().emailId());
client.close();
```

## Key Features

- **Synchronous API** — client methods return concrete response objects and throw `HuefyException` on failure
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
import com.teracrafts.huefy.models.BulkRecipient;
import com.teracrafts.huefy.models.SendBulkEmailsRequest;
import com.teracrafts.huefy.models.SendBulkEmailsResponse;
import java.util.List;

SendBulkEmailsRequest bulk = new SendBulkEmailsRequest(
    "promo",
    List.of(
        new BulkRecipient("bob@example.com", null, null),
        new BulkRecipient("carol@example.com", null, null)
    )
);

SendBulkEmailsResponse result = client.sendBulkEmails(bulk);
System.out.printf("Sent: %d, Failed: %d%n", result.data().successCount(), result.data().failureCount());
```

## Error Handling

```java
import com.teracrafts.huefy.errors.ErrorCode;
import com.teracrafts.huefy.errors.HuefyException;

try {
    SendEmailResponse response = client.sendEmail(request);
    System.out.println("Delivered: " + response.data().emailId());
} catch (HuefyException e) {
    if (e.getCode() == ErrorCode.AUTHENTICATION_ERROR) {
        System.err.println("Invalid API key");
    } else if (e.getCode() == ErrorCode.RATE_LIMIT_ERROR && e.getRetryAfter() != null) {
        System.err.printf("Rate limited. Retry after %dms%n", e.getRetryAfter());
    } else if (e.getCode() == ErrorCode.CIRCUIT_OPEN) {
        System.err.println("Circuit open — service unavailable, backing off");
    } else {
        throw e;
    }
}
```

### Error Code Reference

| Type | Code | Meaning |
|------|------|---------|
| `HuefyException` | `AUTHENTICATION_ERROR` | API key rejected |
| `HuefyException` | `RATE_LIMIT_ERROR` | Rate limit exceeded |
| `HuefyException` | `CIRCUIT_OPEN` | Circuit breaker tripped |
| `HuefyException` | `NETWORK_ERROR`, `TIMEOUT_ERROR`, `SERVICE_UNAVAILABLE` | Transport or upstream failure |
| `HuefyException` | `VALIDATION_ERROR` | Invalid request input |

## Health Check

```java
HealthResponse health = client.healthCheck();
if (!"healthy".equals(health.data().status())) {
    System.err.println("Huefy degraded: " + health.data().status());
}
```

## Local Development

`HUEFY_MODE=local` resolves to `https://api.huefy.on/api/v1/sdk`. If you want to bypass Caddy and hit the raw app port directly, override the base URL to `http://localhost:8080/api/v1/sdk`:

```java
HuefyEmailClient client = HuefyEmailClient.emailBuilder()
    .apiKey("sdk_local_key")
    .baseUrl("https://api.huefy.on/api/v1/sdk")
    .build();
```

## Developer Guide

Full documentation, advanced patterns, and provider configuration are in the [Java Developer Guide](../../docs/spec/guides/java.guide.md).

## License

MIT
