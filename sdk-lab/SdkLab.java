package com.teracrafts.huefy.lab;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.teracrafts.huefy.client.HuefyEmailClient;
import com.teracrafts.huefy.config.HuefyConfig;
import com.teracrafts.huefy.errors.HuefyException;
import com.teracrafts.huefy.models.BulkRecipient;
import com.teracrafts.huefy.models.EmailProvider;
import com.teracrafts.huefy.models.SendBulkEmailsRequest;
import com.teracrafts.huefy.models.SendEmailRequest;
import com.teracrafts.huefy.models.SendEmailResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class SdkLab {

    private static final String GREEN = "\033[32m";
    private static final String RED = "\033[31m";
    private static final String RESET = "\033[0m";
    private static final ObjectMapper JSON = new ObjectMapper();

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== Huefy Java SDK Lab ===");
        System.out.println();

        if (isLiveMode()) {
            runLiveLab();
            printSummary();
            return;
        }

        try (StubServer server = new StubServer()) {
            server.start();

            HuefyEmailClient client = null;
            try {
                client = buildClient(server.baseUrl());
                pass("Initialization");
            } catch (Exception e) {
                fail("Initialization", e.getMessage());
            }

            if (client != null) {
                verifySingleSend(client, server);
                verifyBulkSend(client, server);
                verifyInvalidSingle(client, server);
                verifyInvalidBulk(client, server);
                verifyHealth(client, server);
                verifyCleanup(client);
            }
        }

        System.out.println();
        System.out.println("========================================");
        System.out.printf("Results: %d passed, %d failed%n", passed, failed);
        System.out.println("========================================");
        System.out.println();

        if (failed == 0) {
            System.out.println("All verifications passed!");
        } else {
            System.exit(1);
        }
    }

    private static boolean isLiveMode() {
        return "live".equalsIgnoreCase(System.getenv("HUEFY_SDK_LAB_MODE"));
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(name + " is required in live mode");
        }
        return value.trim();
    }

    private static EmailProvider resolveLiveProvider() {
        String provider = System.getenv("HUEFY_SDK_LIVE_PROVIDER");
        if (provider == null || provider.isBlank()) {
            return null;
        }
        return switch (provider.trim().toLowerCase()) {
            case "sendgrid" -> EmailProvider.SENDGRID;
            case "ses" -> EmailProvider.SES;
            case "mailgun" -> EmailProvider.MAILGUN;
            default -> null;
        };
    }

    private static void runLiveLab() {
        HuefyEmailClient client = null;
        try {
            client = buildClient(requireEnv("HUEFY_SDK_LIVE_BASE_URL"), requireEnv("HUEFY_SDK_LIVE_API_KEY"));
            pass("Initialization");
        } catch (Exception e) {
            fail("Initialization", e.getMessage());
        }

        if (client == null) {
            return;
        }

        String recipient = requireEnv("HUEFY_SDK_LIVE_RECIPIENT");
        String templateKey = requireEnv("HUEFY_SDK_LIVE_TEMPLATE_KEY");
        EmailProvider provider = resolveLiveProvider();

        try {
            SendEmailResponse response = client.sendEmail(new SendEmailRequest(
                    templateKey,
                    Map.of("FirstName", "SDK Live"),
                    recipient,
                    provider
            ));
            if (!response.success()) {
                fail("Single-send live behavior", "expected successful live send");
            } else {
                pass("Single-send live behavior");
            }
        } catch (Exception e) {
            fail("Single-send live behavior", e.getMessage());
        }

        try {
            var response = client.sendBulkEmails(new SendBulkEmailsRequest(
                    templateKey,
                    List.of(new BulkRecipient(recipient, "TO", Map.of())),
                    provider
            ));
            if (!response.success() || response.data().totalRecipients() < 1) {
                fail("Bulk-send live behavior", "expected successful live bulk send");
            } else {
                pass("Bulk-send live behavior");
            }
        } catch (Exception e) {
            fail("Bulk-send live behavior", e.getMessage());
        }

        try {
            client.sendEmail(new SendEmailRequest(templateKey, Map.of(), "bad-email"));
            fail("Invalid single rejection", "expected validation failure");
        } catch (HuefyException e) {
            pass("Invalid single rejection");
        } catch (Exception e) {
            fail("Invalid single rejection", e.getMessage());
        }

        try {
            client.sendBulkEmails(new SendBulkEmailsRequest(
                    templateKey,
                    List.of(new BulkRecipient("bad-email", "reply-to", Map.of()))
            ));
            fail("Invalid bulk rejection", "expected validation failure");
        } catch (HuefyException e) {
            pass("Invalid bulk rejection");
        } catch (Exception e) {
            fail("Invalid bulk rejection", e.getMessage());
        }

        try {
            var health = client.healthCheck();
            if (!"healthy".equals(health.data().status())) {
                fail("Health request path behavior", "expected healthy live response");
            } else {
                pass("Health request path behavior");
            }
        } catch (Exception e) {
            fail("Health request path behavior", e.getMessage());
        }

        try {
            client.close();
            pass("Cleanup");
        } catch (Exception e) {
            fail("Cleanup", e.getMessage());
        }
    }

    private static void printSummary() {
        System.out.println();
        System.out.println("========================================");
        System.out.printf("Results: %d passed, %d failed%n", passed, failed);
        System.out.println("========================================");
        System.out.println();

        if (failed == 0) {
            System.out.println("All verifications passed!");
        } else {
            System.exit(1);
        }
    }

    private static HuefyEmailClient buildClient(String baseUrl) {
        return buildClient(baseUrl, "sdk_lab_test_key_xxxxxxxxxxxx");
    }

    private static HuefyEmailClient buildClient(String baseUrl, String apiKey) {
        return new HuefyEmailClient(HuefyConfig.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .timeout(2_000)
                .retryConfig(new HuefyConfig.RetryConfig(0, 50, 50))
                .build());
    }

    private static void verifySingleSend(HuefyEmailClient client, StubServer server) {
        try {
            SendEmailResponse response = client.sendEmail(new SendEmailRequest(
                    " welcome-email ",
                    Map.of(
                            "name", "John",
                            "count", 2,
                            "beta", true,
                            "roles", List.of("admin", "editor")
                    ),
                    " john@example.com ",
                    EmailProvider.SENDGRID
            ));

            JsonNode body = server.lastBody("/emails/send");
            if (!response.success()) {
                fail("Single-send contract", "stub response was not parsed as success");
                return;
            }
            assertEquals("/emails/send", server.lastPath("/emails/send"), "single-send path");
            assertEquals("welcome-email", body.get("templateKey").asText(), "single-send templateKey");
            assertEquals("john@example.com", body.get("recipient").asText(), "single-send recipient");
            assertEquals("sendgrid", body.get("providerType").asText(), "single-send providerType");
            assertMissing(body, "template_key", "single-send legacy template key");
            assertMissing(body, "provider", "single-send legacy provider key");
            assertEquals("John", body.get("data").get("name").asText(), "single-send data.name");
            assertEquals(2, body.get("data").get("count").asInt(), "single-send data.count");
            assertTrue(body.get("data").get("beta").asBoolean(), "single-send data.beta");
            assertEquals("admin", body.get("data").get("roles").get(0).asText(), "single-send data.roles[0]");
            pass("Single-send contract shaping");
        } catch (Exception e) {
            fail("Single-send contract shaping", e.getMessage());
        }
    }

    private static void verifyBulkSend(HuefyEmailClient client, StubServer server) {
        try {
            client.sendBulkEmails(new SendBulkEmailsRequest(
                    " account-update ",
                    List.of(
                            new BulkRecipient(" alice@example.com ", "TO", Map.of("segment", "vip")),
                            new BulkRecipient("bob@example.com", "cc", Map.of("segment", "standard"))
                    ),
                    EmailProvider.SES
            ));

            JsonNode body = server.lastBody("/emails/send-bulk");
            assertEquals("/emails/send-bulk", server.lastPath("/emails/send-bulk"), "bulk-send path");
            assertEquals("account-update", body.get("templateKey").asText(), "bulk-send templateKey");
            assertEquals("ses", body.get("providerType").asText(), "bulk-send providerType");
            assertEquals("alice@example.com", body.get("recipients").get(0).get("email").asText(), "bulk recipient 0 email");
            assertEquals("to", body.get("recipients").get(0).get("type").asText(), "bulk recipient 0 type");
            assertEquals("vip", body.get("recipients").get(0).get("data").get("segment").asText(), "bulk recipient 0 data");
            assertEquals("cc", body.get("recipients").get(1).get("type").asText(), "bulk recipient 1 type");
            pass("Bulk-send contract shaping");
        } catch (Exception e) {
            fail("Bulk-send contract shaping", e.getMessage());
        }
    }

    private static void verifyInvalidSingle(HuefyEmailClient client, StubServer server) {
        int before = server.hitCount("/emails/send");
        try {
            client.sendEmail(new SendEmailRequest(
                    "welcome",
                    Map.of("name", "John"),
                    "not-an-email"
            ));
            fail("Invalid single rejection", "expected validation failure");
        } catch (HuefyException e) {
            int after = server.hitCount("/emails/send");
            if (after != before) {
                fail("Invalid single rejection", "transport was called for invalid single input");
                return;
            }
            if (!e.getMessage().contains("Validation failed")) {
                fail("Invalid single rejection", "unexpected error: " + e.getMessage());
                return;
            }
            pass("Invalid single rejection");
        } catch (Exception e) {
            fail("Invalid single rejection", e.getMessage());
        }
    }

    private static void verifyInvalidBulk(HuefyEmailClient client, StubServer server) {
        int before = server.hitCount("/emails/send-bulk");
        try {
            client.sendBulkEmails(new SendBulkEmailsRequest(
                    "welcome",
                    List.of(new BulkRecipient("john@example.com", "reply-to", Map.of("segment", "vip")))
            ));
            fail("Invalid bulk rejection", "expected validation failure");
        } catch (HuefyException e) {
            if (server.hitCount("/emails/send-bulk") != before) {
                fail("Invalid bulk rejection", "transport was called for invalid bulk input");
                return;
            }
            if (!e.getMessage().contains("recipients[0]")) {
                fail("Invalid bulk rejection", "unexpected error: " + e.getMessage());
                return;
            }
            pass("Invalid bulk rejection");
        } catch (Exception e) {
            fail("Invalid bulk rejection", e.getMessage());
        }
    }

    private static void verifyHealth(HuefyEmailClient client, StubServer server) {
        try {
            var health = client.healthCheck();
            assertEquals("/health", server.lastPath("/health"), "health path");
            assertEquals("healthy", health.data().status(), "health status");
            pass("Health request path behavior");
        } catch (Exception e) {
            fail("Health request path behavior", e.getMessage());
        }
    }

    private static void verifyCleanup(HuefyEmailClient client) {
        try {
            client.close();
            client.healthCheck();
            fail("Cleanup", "expected closed client to reject requests");
        } catch (HuefyException e) {
            if (e.getMessage().contains("closed")) {
                pass("Cleanup");
            } else {
                fail("Cleanup", e.getMessage());
            }
        } catch (Exception e) {
            fail("Cleanup", e.getMessage());
        }
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if ((expected == null && actual != null) || (expected != null && !expected.equals(actual))) {
            throw new IllegalStateException(label + " expected " + expected + " but got " + actual);
        }
    }

    private static void assertTrue(boolean value, String label) {
        if (!value) {
            throw new IllegalStateException(label + " expected true");
        }
    }

    private static void assertMissing(JsonNode body, String field, String label) {
        if (body.has(field)) {
            throw new IllegalStateException(label + " should be absent");
        }
    }

    private static void pass(String label) {
        passed++;
        System.out.printf("%s[PASS]%s %s%n", GREEN, RESET, label);
    }

    private static void fail(String label, String reason) {
        failed++;
        System.out.printf("%s[FAIL]%s %s - %s%n", RED, RESET, label, reason);
    }

    private static final class StubServer implements AutoCloseable {
        private final HttpServer server;
        private final Map<String, JsonNode> lastBodies = new java.util.HashMap<>();
        private final Map<String, String> lastPaths = new java.util.HashMap<>();
        private final Map<String, Integer> hitCounts = new java.util.HashMap<>();

        private StubServer() throws IOException {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/emails/send", exchange -> handle(exchange, singleSendResponse()));
            server.createContext("/emails/send-bulk", exchange -> handle(exchange, bulkSendResponse()));
            server.createContext("/health", exchange -> handle(exchange, healthResponse()));
        }

        void start() {
            server.start();
        }

        String baseUrl() {
            return "http://127.0.0.1:" + server.getAddress().getPort();
        }

        int hitCount(String path) {
            return hitCounts.getOrDefault(path, 0);
        }

        String lastPath(String path) {
            return lastPaths.get(path);
        }

        JsonNode lastBody(String path) {
            return lastBodies.get(path);
        }

        private void handle(HttpExchange exchange, String responseBody) throws IOException {
            String path = exchange.getRequestURI().getPath();
            hitCounts.put(path, hitCount(path) + 1);
            lastPaths.put(path, path);
            String requestBody = readBody(exchange.getRequestBody());
            if (!requestBody.isBlank()) {
                lastBodies.put(path, JSON.readTree(requestBody));
            }

            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        }

        private static String readBody(InputStream inputStream) throws IOException {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }

        private static String singleSendResponse() {
            return """
                    {"success":true,"data":{"emailId":"email_123","status":"queued","recipients":[{"email":"john@example.com","status":"queued","messageId":"msg_123","sentAt":"2026-01-01T00:00:00Z"}],"scheduledAt":null,"sentAt":null},"correlationId":"corr_single"}
                    """;
        }

        private static String bulkSendResponse() {
            return """
                    {"success":true,"data":{"batchId":"batch_123","status":"queued","templateKey":"account-update","templateVersion":1,"senderUsed":"noreply@example.com","senderVerified":true,"totalRecipients":2,"processedCount":2,"successCount":2,"failureCount":0,"suppressedCount":0,"startedAt":"2026-01-01T00:00:00Z","completedAt":"2026-01-01T00:00:01Z","recipients":[{"email":"alice@example.com","status":"queued","messageId":"msg_1","sentAt":"2026-01-01T00:00:00Z"},{"email":"bob@example.com","status":"queued","messageId":"msg_2","sentAt":"2026-01-01T00:00:00Z"}],"errors":[],"metadata":{"source":"sdk-lab"}},"correlationId":"corr_bulk"}
                    """;
        }

        private static String healthResponse() {
            return """
                    {"success":true,"data":{"status":"healthy","timestamp":"2026-01-01T00:00:00Z","version":"sdk-lab"},"correlationId":"corr_health"}
                    """;
        }

        @Override
        public void close() {
            server.stop(0);
        }
    }
}
