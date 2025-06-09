# Huefy Java SDK

The official Java SDK for the Huefy email sending platform. Send template-based emails with support for multiple providers, automatic retries, and comprehensive error handling.

## Installation

### Maven

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.huefy</groupId>
    <artifactId>huefy-java-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

Add the following to your `build.gradle`:

```groovy
implementation 'com.huefy:huefy-java-sdk:1.0.0'
```

## Quick Start

```java
import com.huefy.sdk.HuefyClient;
import com.huefy.sdk.models.*;
import java.util.Map;

public class Example {
    public static void main(String[] args) throws Exception {
        // Create client
        HuefyClient client = new HuefyClient("your-api-key");
        
        // Send email
        SendEmailRequest request = SendEmailRequest.builder()
            .templateKey("welcome-email")
            .recipient("john@example.com")
            .data(Map.of(
                "name", "John Doe",
                "company", "Acme Corp"
            ))
            .build();
        
        SendEmailResponse response = client.sendEmail(request);
        System.out.println("Email sent: " + response.getMessageId());
        
        // Clean up
        client.close();
    }
}
```

## Features

- ✅ **Template-based emails** - Send emails using predefined templates
- ✅ **Multiple providers** - Support for SES, SendGrid, Mailgun, Mailchimp
- ✅ **Automatic retries** - Configurable retry logic with exponential backoff
- ✅ **Error handling** - Comprehensive exception types for different failure scenarios
- ✅ **Bulk emails** - Send multiple emails in a single request
- ✅ **Health checks** - Monitor API health status
- ✅ **Thread safety** - Safe to use in multi-threaded environments
- ✅ **Java 11+** - Compatible with Java 11 and later versions

## Configuration

### Basic Configuration

```java
HuefyClient client = new HuefyClient("your-api-key");
```

### Advanced Configuration

```java
import java.time.Duration;

HuefyClientConfig config = HuefyClientConfig.builder()
    .baseUrl("https://api.huefy.com")
    .connectTimeout(Duration.ofSeconds(10))
    .readTimeout(Duration.ofSeconds(30))
    .retryConfig(RetryConfig.builder()
        .enabled(true)
        .maxRetries(5)
        .baseDelay(Duration.ofMillis(500))
        .maxDelay(Duration.ofSeconds(10))
        .multiplier(2.0)
        .build())
    .build();

HuefyClient client = new HuefyClient("your-api-key", config);
```

## API Reference

### Client Creation

#### `new HuefyClient(String apiKey)`

Creates a new Huefy client with the provided API key and default configuration.

#### `new HuefyClient(String apiKey, HuefyClientConfig config)`

Creates a new Huefy client with the provided API key and custom configuration.

### Email Operations

#### `SendEmailResponse sendEmail(SendEmailRequest request)`

Sends a single email using a template.

```java
SendEmailRequest request = SendEmailRequest.builder()
    .templateKey("welcome-email")
    .recipient("john@example.com")
    .data(Map.of(
        "name", "John Doe",
        "company", "Acme Corp"
    ))
    .provider(EmailProvider.SENDGRID) // Optional
    .build();

SendEmailResponse response = client.sendEmail(request);
```

#### `BulkEmailResponse sendBulkEmails(List<SendEmailRequest> requests)`

Sends multiple emails in a single request.

```java
List<SendEmailRequest> requests = List.of(
    SendEmailRequest.builder()
        .templateKey("welcome-email")
        .recipient("john@example.com")
        .data(Map.of("name", "John Doe"))
        .build(),
    SendEmailRequest.builder()
        .templateKey("welcome-email")
        .recipient("jane@example.com")
        .data(Map.of("name", "Jane Doe"))
        .build()
);

BulkEmailResponse response = client.sendBulkEmails(requests);
```

#### `HealthResponse healthCheck()`

Checks the API health status.

```java
HealthResponse health = client.healthCheck();
System.out.println("API Status: " + health.getStatus());
```

### Resource Management

#### `void close()`

Closes the client and releases resources. Always call this when done with the client.

```java
try (HuefyClient client = new HuefyClient("api-key")) {
    // Use client
} // Automatically closed
```

## Error Handling

The SDK provides specific exception types for different failure scenarios:

```java
try {
    SendEmailResponse response = client.sendEmail(request);
} catch (AuthenticationException e) {
    // Handle authentication failures
    System.err.println("Authentication failed: " + e.getMessage());
} catch (TemplateNotFoundException e) {
    // Handle template not found
    System.err.println("Template '" + e.getTemplateKey() + "' not found");
} catch (RateLimitException e) {
    // Handle rate limiting
    System.err.println("Rate limited. Retry after " + e.getRetryAfter() + " seconds");
} catch (ProviderException e) {
    // Handle provider-specific errors
    System.err.println("Provider " + e.getProvider() + " error: " + e.getProviderCode());
} catch (NetworkException e) {
    // Handle network errors (automatically retried)
    System.err.println("Network error: " + e.getMessage());
} catch (HuefyException e) {
    // Handle other Huefy errors
    System.err.println("Huefy error: " + e.getMessage());
}
```

### Exception Types

- `AuthenticationException` - Invalid API key or authentication failure
- `TemplateNotFoundException` - Specified template doesn't exist
- `InvalidTemplateDataException` - Template data validation failed
- `InvalidRecipientException` - Invalid recipient email address
- `ProviderException` - Email provider rejected the message
- `RateLimitException` - Rate limit exceeded
- `NetworkException` - Network connectivity issues
- `TimeoutException` - Request timeout
- `ValidationException` - Request validation failed
- `HuefyException` - Base exception for all SDK errors

## Email Providers

Supported email providers:

```java
EmailProvider.SES       // Amazon SES
EmailProvider.SENDGRID  // SendGrid
EmailProvider.MAILGUN   // Mailgun
EmailProvider.MAILCHIMP // Mailchimp Transactional
```

## Retry Configuration

Configure automatic retry behavior for failed requests:

```java
RetryConfig retryConfig = RetryConfig.builder()
    .enabled(true)                              // Enable retries
    .maxRetries(3)                              // Maximum number of retries
    .baseDelay(Duration.ofSeconds(1))           // Initial delay between retries
    .maxDelay(Duration.ofSeconds(30))           // Maximum delay between retries
    .multiplier(2.0)                            // Delay multiplier for exponential backoff
    .build();

HuefyClientConfig config = HuefyClientConfig.builder()
    .retryConfig(retryConfig)
    .build();

HuefyClient client = new HuefyClient("api-key", config);
```

Retries are automatically performed for:
- Network errors
- Timeout errors
- Rate limit errors (429)
- Server errors (5xx)

## Examples

See the [examples](src/examples/) directory for complete examples:

- [Basic Usage](src/examples/java/com/huefy/examples/BasicExample.java) - Simple email sending
- [Bulk Emails](src/examples/java/com/huefy/examples/BulkExample.java) - Sending multiple emails
- [Advanced Usage](src/examples/java/com/huefy/examples/AdvancedExample.java) - Custom configuration and error handling

## Testing

Run the test suite:

```bash
mvn test
```

Run tests with coverage:

```bash
mvn test jacoco:report
```

View coverage report:

```bash
open target/site/jacoco/index.html
```

## Building

Build the project:

```bash
mvn clean compile
```

Build JAR:

```bash
mvn clean package
```

Build with all artifacts (sources, javadoc):

```bash
mvn clean package -P release
```

## Requirements

- Java 11 or later
- Valid Huefy API key

## Dependencies

- [OkHttp](https://square.github.io/okhttp/) - HTTP client
- [Jackson](https://github.com/FasterXML/jackson) - JSON processing
- [SLF4J](http://www.slf4j.org/) - Logging facade

## License

MIT License - see [LICENSE](LICENSE) file for details.

## Support

For support and questions:

- 📧 Email: support@huefy.com
- 📖 Documentation: https://docs.huefy.com
- 🐛 Issues: https://github.com/huefy/huefy-sdk/issues