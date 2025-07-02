package dev.huefy.sdk.examples;

import dev.huefy.sdk.HuefyClient;
import dev.huefy.sdk.HuefyClientConfig;
import dev.huefy.sdk.RetryConfig;
import dev.huefy.sdk.exceptions.*;
import dev.huefy.sdk.models.*;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Basic usage examples for the Huefy Java SDK.
 * 
 * This example demonstrates:
 * 1. Creating a Huefy client
 * 2. Sending single emails
 * 3. Sending bulk emails
 * 4. Error handling
 * 5. Health checks
 * 6. Async operations
 * 7. Custom configurations
 */
public class BasicUsage {
    
    // Replace with your actual API key
    private static final String API_KEY = System.getenv("HUEFY_API_KEY") != null 
        ? System.getenv("HUEFY_API_KEY") 
        : "your-huefy-api-key";

    public static void main(String[] args) {
        try {
            // Example 1: Basic client creation and single email
            System.out.println("=== Basic Email Sending ===");
            basicEmailExample();

            // Example 2: Bulk email sending
            System.out.println("\n=== Bulk Email Sending ===");
            bulkEmailExample();

            // Example 3: Health check
            System.out.println("\n=== API Health Check ===");
            healthCheckExample();

            // Example 4: Using different email providers
            System.out.println("\n=== Multiple Email Providers ===");
            multipleProvidersExample();

            // Example 5: Custom configuration
            System.out.println("\n=== Custom Configuration ===");
            customConfigurationExample();

            // Example 6: Async operations
            System.out.println("\n=== Async Operations ===");
            asyncOperationsExample();

            // Example 7: Error handling
            System.out.println("\n=== Error Handling Examples ===");
            errorHandlingExample();

        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n=== Java example completed ===");
    }

    private static void basicEmailExample() throws HuefyException {
        HuefyClient client = new HuefyClient(API_KEY);

        Map<String, Object> emailData = new HashMap<>();
        emailData.put("name", "John Doe");
        emailData.put("company", "Acme Corporation");
        emailData.put("activationLink", "https://app.example.com/activate/abc123");
        emailData.put("supportEmail", "support@example.com");

        SendEmailRequest request = SendEmailRequest.builder()
            .templateKey("welcome-email")
            .recipient("john@example.com")
            .data(emailData)
            .provider(EmailProvider.SENDGRID)
            .build();

        SendEmailResponse response = client.sendEmail(request);

        System.out.println("✅ Email sent successfully!");
        System.out.println("Message ID: " + response.getMessageId());
        System.out.println("Provider: " + response.getProvider().getDisplayName());
        System.out.println("Status: " + response.getStatus());
    }

    private static void bulkEmailExample() throws HuefyException {
        HuefyClient client = new HuefyClient(API_KEY);

        List<User> users = Arrays.asList(
            new User("Alice Johnson", "alice@example.com", "Tech Corp"),
            new User("Bob Smith", "bob@example.com", "Startup Inc"),
            new User("Carol Davis", "carol@example.com", null)
        );

        List<SendEmailRequest> requests = new ArrayList<>();
        
        for (User user : users) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("name", user.getName());
            userData.put("company", user.getCompany() != null ? user.getCompany() : "Your Organization");
            userData.put("activationLink", "https://app.example.com/activate/" + System.currentTimeMillis());
            userData.put("supportEmail", "support@example.com");

            SendEmailRequest request = SendEmailRequest.builder()
                .templateKey("welcome-email")
                .recipient(user.getEmail())
                .data(userData)
                .provider(EmailProvider.SENDGRID)
                .build();

            requests.add(request);
        }

        BulkEmailResponse response = client.sendBulkEmails(requests);

        System.out.println("✅ Bulk email operation completed!");
        System.out.println("Total emails: " + response.getTotalEmails());
        System.out.println("Successful: " + response.getSuccessfulEmails());
        System.out.println("Failed: " + response.getFailedEmails());
        System.out.printf("Success rate: %.1f%%\n", response.getSuccessRate());

        if (response.getFailedEmails() > 0) {
            System.out.println("❌ Failed emails:");
            response.getFailedResults().forEach(result -> 
                System.out.println("  - " + result.get("recipient") + ": " + result.get("error"))
            );
        }
    }

    private static void healthCheckExample() throws HuefyException {
        HuefyClient client = new HuefyClient(API_KEY);
        
        HealthResponse health = client.healthCheck();

        switch (health.getStatus().toLowerCase()) {
            case "healthy":
                System.out.println("✅ API is healthy");
                break;
            case "degraded":
                System.out.println("⚠️ API is degraded");
                break;
            default:
                System.out.println("❌ API is unhealthy");
                break;
        }

        System.out.println("Version: " + health.getVersion());
        System.out.println("Uptime: " + (health.getUptime() / 3600) + " hours");
        System.out.println("Timestamp: " + health.getTimestamp());
    }

    private static void multipleProvidersExample() {
        HuefyClient client = new HuefyClient(API_KEY);

        EmailProvider[] providers = {
            EmailProvider.SENDGRID,
            EmailProvider.MAILGUN,
            EmailProvider.SES,
            EmailProvider.MAILCHIMP
        };

        Map<String, Object> testData = new HashMap<>();
        testData.put("message", "Testing provider functionality");
        testData.put("timestamp", new Date().toString());

        for (EmailProvider provider : providers) {
            try {
                SendEmailRequest request = SendEmailRequest.builder()
                    .templateKey("test-template")
                    .recipient("test@example.com")
                    .data(testData)
                    .provider(provider)
                    .build();

                SendEmailResponse response = client.sendEmail(request);
                System.out.println("✅ " + provider.getDisplayName() + ": " + response.getMessageId());

            } catch (HuefyException e) {
                System.out.println("❌ " + provider.getDisplayName() + ": " + e.getMessage());
            }
        }
    }

    private static void customConfigurationExample() throws HuefyException {
        RetryConfig retryConfig = RetryConfig.builder()
            .maxRetries(5)
            .baseDelay(Duration.ofSeconds(2))
            .maxDelay(Duration.ofMinutes(1))
            .build();

        HuefyConfig config = HuefyConfig.builder()
            .baseUrl("https://api.huefy.com")
            .timeout(Duration.ofSeconds(45))
            .connectTimeout(Duration.ofSeconds(15))
            .retryConfig(retryConfig)
            .userAgent("MyApp/1.0")
            .build();

        HuefyClient client = new HuefyClient(API_KEY, config);

        Map<String, Object> emailData = new HashMap<>();
        emailData.put("username", "johndoe");
        emailData.put("resetLink", "https://app.example.com/reset/xyz789");
        emailData.put("expiresAt", "2024-01-02 15:30:00");

        SendEmailRequest request = SendEmailRequest.builder()
            .templateKey("password-reset")
            .recipient("user@example.com")
            .data(emailData)
            .provider(EmailProvider.MAILGUN)
            .build();

        SendEmailResponse response = client.sendEmail(request);
        System.out.println("✅ Password reset email sent: " + response.getMessageId());
    }

    private static void asyncOperationsExample() {
        HuefyClient client = new HuefyClient(API_KEY);

        Map<String, Object> emailData = new HashMap<>();
        emailData.put("name", "Async User");
        emailData.put("message", "This email was sent asynchronously");

        SendEmailRequest request = SendEmailRequest.builder()
            .templateKey("async-test")
            .recipient("async@example.com")
            .data(emailData)
            .provider(EmailProvider.SES)
            .build();

        // Send email asynchronously
        CompletableFuture<SendEmailResponse> future = client.sendEmailAsync(request);

        future.thenAccept(response -> {
            System.out.println("✅ Async email sent: " + response.getMessageId());
        }).exceptionally(throwable -> {
            System.err.println("❌ Async email failed: " + throwable.getMessage());
            return null;
        });

        // Wait for completion (in real applications, you might not want to block)
        try {
            future.get();
        } catch (Exception e) {
            System.err.println("Error waiting for async operation: " + e.getMessage());
        }
    }

    private static void errorHandlingExample() {
        HuefyClient client = new HuefyClient(API_KEY);

        // Example 1: Validation error
        try {
            SendEmailRequest invalidRequest = SendEmailRequest.builder()
                .templateKey("") // Invalid empty template key
                .recipient("test@example.com")
                .data(Map.of("message", "Test"))
                .build();

            client.sendEmail(invalidRequest);
        } catch (ValidationException e) {
            System.out.println("Validation Error: " + e.getMessage());
            if (e.getField() != null) {
                System.out.println("Field: " + e.getField());
            }
        } catch (HuefyException e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }

        // Example 2: Invalid email address
        try {
            SendEmailRequest invalidEmailRequest = SendEmailRequest.builder()
                .templateKey("test-template")
                .recipient("invalid-email-address")
                .data(Map.of("message", "Test"))
                .build();

            client.sendEmail(invalidEmailRequest);
        } catch (ValidationException e) {
            System.out.println("Email validation error: " + e.getMessage());
        } catch (HuefyException e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }

        // Example 3: Authentication error (using invalid API key)
        try {
            HuefyClient invalidClient = new HuefyClient("invalid-api-key");
            
            SendEmailRequest request = SendEmailRequest.builder()
                .templateKey("test-template")
                .recipient("test@example.com")
                .data(Map.of("message", "Test"))
                .build();

            invalidClient.sendEmail(request);
        } catch (AuthenticationException e) {
            System.out.println("Authentication Error: " + e.getMessage());
            System.out.println("💡 Please check your API key configuration.");
        } catch (HuefyException e) {
            System.out.println("Unexpected error: " + e.getMessage());
        }

        // Example 4: Timeout handling
        try {
            HuefyConfig timeoutConfig = HuefyConfig.builder()
                .timeout(Duration.ofMillis(1)) // Very short timeout
                .build();

            HuefyClient timeoutClient = new HuefyClient(API_KEY, timeoutConfig);
            
            SendEmailRequest request = SendEmailRequest.builder()
                .templateKey("test-template")
                .recipient("timeout-test@example.com")
                .data(Map.of("message", "This will timeout"))
                .build();

            timeoutClient.sendEmail(request);
        } catch (TimeoutException e) {
            System.out.println("Expected timeout occurred: " + e.getMessage());
        } catch (NetworkException e) {
            System.out.println("Network error: " + e.getMessage());
        } catch (HuefyException e) {
            System.out.println("Other error: " + e.getMessage());
        }
    }

    // Helper classes
    private static class User {
        private final String name;
        private final String email;
        private final String company;

        public User(String name, String email, String company) {
            this.name = name;
            this.email = email;
            this.company = company;
        }

        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getCompany() { return company; }
    }

    // Utility method for error handling
    private static void handleEmailError(HuefyException e, String operation) {
        System.out.printf("❌ %s failed: ", operation);

        if (e instanceof ValidationException) {
            ValidationException ve = (ValidationException) e;
            System.out.print("Validation error - " + ve.getMessage());
            if (ve.getField() != null) {
                System.out.print(" (Field: " + ve.getField() + ")");
            }
        } else if (e instanceof AuthenticationException) {
            System.out.print("Authentication error - " + e.getMessage());
            System.out.println("\n💡 Please check your API key configuration.");
        } else if (e instanceof NetworkException) {
            System.out.print("Network error - " + e.getMessage());
            System.out.println("\n💡 Please check your network connection.");
        } else if (e instanceof TimeoutException) {
            System.out.print("Timeout error - " + e.getMessage());
            System.out.println("\n💡 Consider increasing timeout settings.");
        } else {
            System.out.print("Unknown error - " + e.getMessage());
        }
        System.out.println();
    }
}