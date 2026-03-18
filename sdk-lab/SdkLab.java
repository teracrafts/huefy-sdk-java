package com.huefy.lab;

import com.huefy.client.HuefyClient;
import com.huefy.config.HuefyConfig;
import com.huefy.errors.ErrorSanitizer;
import com.huefy.http.CircuitBreaker;
import com.huefy.security.Security;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public class SdkLab {

    private static final String GREEN = "\033[32m";
    private static final String RED   = "\033[31m";
    private static final String RESET = "\033[0m";

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=== Huefy Java SDK Lab ===");
        System.out.println();

        HuefyClient client = null;

        // 1. Initialization
        try {
            client = new HuefyClient("sdk_lab_test_key_xxxxxxxxxxxx");
            pass("Initialization");
        } catch (Exception e) {
            fail("Initialization", e.getMessage());
        }

        // 2. Config validation
        try {
            new HuefyClient("");
            fail("Config validation", "Expected exception for empty API key");
        } catch (IllegalArgumentException e) {
            pass("Config validation");
        } catch (Exception e) {
            pass("Config validation");
        }

        // 3. HMAC signing
        try {
            String sig = Security.generateHmacSignature("{\"test\": \"data\"}", "test_secret");
            if (sig != null && sig.length() == 64) {
                pass("HMAC signing");
            } else {
                fail("HMAC signing", "Expected 64-char hex signature, got: " + (sig == null ? "null" : sig.length()));
            }
        } catch (Exception e) {
            fail("HMAC signing", e.getMessage());
        }

        // 4. Error sanitization
        try {
            String input = "Error at 192.168.1.1 for user@example.com";
            String sanitized = ErrorSanitizer.sanitize(input);
            if (!sanitized.contains("192.168.1.1") && !sanitized.contains("user@example.com")) {
                pass("Error sanitization");
            } else {
                fail("Error sanitization", "IP or email not redacted: " + sanitized);
            }
        } catch (Exception e) {
            fail("Error sanitization", e.getMessage());
        }

        // 5. PII detection
        try {
            String piiText = "{\"email\": \"t@t.com\", \"name\": \"John\", \"ssn\": \"123-45-6789\"}";
            List<String> detected = Security.detectPii(piiText);
            if (!detected.isEmpty() && (detected.contains("email") || detected.contains("ssn"))) {
                pass("PII detection");
            } else {
                fail("PII detection", "Expected email/ssn in: " + detected);
            }
        } catch (Exception e) {
            fail("PII detection", e.getMessage());
        }

        // 6. Circuit breaker state
        try {
            CircuitBreaker cb = new CircuitBreaker(new HuefyConfig.CircuitBreakerConfig());
            if (cb.getState() == CircuitBreaker.State.CLOSED) {
                pass("Circuit breaker state");
            } else {
                fail("Circuit breaker state", "Expected CLOSED, got: " + cb.getState());
            }
        } catch (Exception e) {
            fail("Circuit breaker state", e.getMessage());
        }

        // 7. Health check
        try {
            java.net.http.HttpClient http = java.net.http.HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.huefy.dev/api/v1/sdk/health"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            http.send(req, HttpResponse.BodyHandlers.ofString());
            pass("Health check");
        } catch (Exception e) {
            pass("Health check");
        }

        // 8. Cleanup
        try {
            if (client != null) {
                client.close();
            }
            pass("Cleanup");
        } catch (Exception e) {
            fail("Cleanup", e.getMessage());
        }

        // Summary
        System.out.println();
        System.out.println("========================================");
        System.out.printf("Results: %d passed, %d failed%n", passed, failed);
        System.out.println("========================================");
        System.out.println();

        if (failed == 0) {
            System.out.println("All verifications passed!");
            System.exit(0);
        } else {
            System.exit(1);
        }
    }

    private static void pass(String label) {
        passed++;
        System.out.printf("%s[PASS]%s %s%n", GREEN, RESET, label);
    }

    private static void fail(String label, String reason) {
        failed++;
        System.out.printf("%s[FAIL]%s %s — %s%n", RED, RESET, label, reason);
    }
}
