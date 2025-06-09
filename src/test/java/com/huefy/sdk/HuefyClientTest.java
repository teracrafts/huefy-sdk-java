package com.huefy.sdk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.huefy.sdk.exceptions.*;
import com.huefy.sdk.models.*;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HuefyClientTest {
    
    private MockWebServer mockWebServer;
    private HuefyClient client;
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        String baseUrl = mockWebServer.url("/").toString();
        HuefyClientConfig config = HuefyClientConfig.builder()
            .baseUrl(baseUrl)
            .connectTimeout(Duration.ofSeconds(5))
            .readTimeout(Duration.ofSeconds(5))
            .retryConfig(RetryConfig.builder().enabled(false).build()) // Disable retries for tests
            .build();
        
        client = new HuefyClient("test-api-key", config);
    }
    
    @AfterEach
    void tearDown() throws IOException {
        client.close();
        mockWebServer.shutdown();
    }
    
    @Test
    void constructor_withNullApiKey_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> new HuefyClient(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("API key cannot be null or empty");
    }
    
    @Test
    void constructor_withEmptyApiKey_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> new HuefyClient(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("API key cannot be null or empty");
    }
    
    @Test
    void constructor_withWhitespaceApiKey_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> new HuefyClient("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("API key cannot be null or empty");
    }
    
    @Test
    void sendEmail_withValidRequest_returnsSuccessResponse() throws Exception {
        // Arrange
        SendEmailResponse expectedResponse = new SendEmailResponse(
            "msg-123",
            "sent",
            EmailProvider.SES,
            Instant.now()
        );
        
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody(objectMapper.writeValueAsString(expectedResponse))
            .addHeader("Content-Type", "application/json"));
        
        SendEmailRequest request = SendEmailRequest.builder()
            .templateKey("welcome-email")
            .recipient("john@example.com")
            .data(Map.of("name", "John Doe"))
            .build();
        
        // Act
        SendEmailResponse response = client.sendEmail(request);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getMessageId()).isEqualTo("msg-123");
        assertThat(response.getStatus()).isEqualTo("sent");
        assertThat(response.getProvider()).isEqualTo(EmailProvider.SES);
        
        // Verify request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/api/v1/sdk/emails/send");
        assertThat(recordedRequest.getHeader("X-API-Key")).isEqualTo("test-api-key");
        assertThat(recordedRequest.getHeader("Content-Type")).isEqualTo("application/json; charset=utf-8");
        assertThat(recordedRequest.getHeader("User-Agent")).contains("Huefy-Java-SDK");
    }
    
    @Test
    void sendEmail_withNullRequest_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> client.sendEmail(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Request cannot be null");
    }
    
    @Test
    void sendEmail_withInvalidRequest_throwsIllegalArgumentException() {
        SendEmailRequest request = SendEmailRequest.builder()
            .templateKey("") // Invalid: empty template key
            .recipient("john@example.com")
            .data(Map.of("name", "John Doe"))
            .build();
        
        assertThatThrownBy(() -> client.sendEmail(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Template key is required");
    }
    
    @Test
    void sendEmail_withAuthenticationError_throwsAuthenticationException() throws Exception {
        // Arrange
        ErrorResponse errorResponse = new ErrorResponse(
            new ErrorDetail("AUTHENTICATION_FAILED", "Invalid API key", null)
        );
        
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(401)
            .setBody(objectMapper.writeValueAsString(errorResponse))
            .addHeader("Content-Type", "application/json"));
        
        SendEmailRequest request = SendEmailRequest.builder()
            .templateKey("welcome-email")
            .recipient("john@example.com")
            .data(Map.of("name", "John Doe"))
            .build();
        
        // Act & Assert
        assertThatThrownBy(() -> client.sendEmail(request))
            .isInstanceOf(AuthenticationException.class)
            .hasMessageContaining("Invalid API key");
    }
    
    @Test
    void sendEmail_withTemplateNotFoundError_throwsTemplateNotFoundException() throws Exception {
        // Arrange
        ErrorResponse errorResponse = new ErrorResponse(
            new ErrorDetail(
                "TEMPLATE_NOT_FOUND",
                "Template not found: nonexistent-template",
                Map.of("templateKey", "nonexistent-template")
            )
        );
        
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(404)
            .setBody(objectMapper.writeValueAsString(errorResponse))
            .addHeader("Content-Type", "application/json"));
        
        SendEmailRequest request = SendEmailRequest.builder()
            .templateKey("nonexistent-template")
            .recipient("john@example.com")
            .data(Map.of("name", "John Doe"))
            .build();
        
        // Act & Assert
        assertThatThrownBy(() -> client.sendEmail(request))
            .isInstanceOf(TemplateNotFoundException.class)
            .hasMessageContaining("Template not found")
            .satisfies(ex -> {
                TemplateNotFoundException templateEx = (TemplateNotFoundException) ex;
                assertThat(templateEx.getTemplateKey()).isEqualTo("nonexistent-template");
            });
    }
    
    @Test
    void sendEmail_withRateLimitError_throwsRateLimitException() throws Exception {
        // Arrange
        ErrorResponse errorResponse = new ErrorResponse(
            new ErrorDetail(
                "RATE_LIMIT_EXCEEDED",
                "Rate limit exceeded",
                Map.of("retryAfter", 60)
            )
        );
        
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(429)
            .setBody(objectMapper.writeValueAsString(errorResponse))
            .addHeader("Content-Type", "application/json"));
        
        SendEmailRequest request = SendEmailRequest.builder()
            .templateKey("welcome-email")
            .recipient("john@example.com")
            .data(Map.of("name", "John Doe"))
            .build();
        
        // Act & Assert
        assertThatThrownBy(() -> client.sendEmail(request))
            .isInstanceOf(RateLimitException.class)
            .hasMessageContaining("Rate limit exceeded")
            .satisfies(ex -> {
                RateLimitException rateLimitEx = (RateLimitException) ex;
                assertThat(rateLimitEx.getRetryAfter()).isEqualTo(60);
            });
    }
    
    @Test
    void sendBulkEmails_withValidRequests_returnsSuccessResponse() throws Exception {
        // Arrange
        BulkEmailResponse expectedResponse = new BulkEmailResponse(
            List.of(
                new BulkEmailResponse.BulkEmailResult(
                    new SendEmailResponse("msg-123", "sent", EmailProvider.SES, Instant.now())
                ),
                new BulkEmailResponse.BulkEmailResult(
                    new SendEmailResponse("msg-456", "sent", EmailProvider.SES, Instant.now())
                )
            )
        );
        
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody(objectMapper.writeValueAsString(expectedResponse))
            .addHeader("Content-Type", "application/json"));
        
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
        
        // Act
        BulkEmailResponse response = client.sendBulkEmails(requests);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getResults()).hasSize(2);
        assertThat(response.getResults().get(0).isSuccess()).isTrue();
        assertThat(response.getResults().get(1).isSuccess()).isTrue();
        
        // Verify request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/api/v1/sdk/emails/bulk");
    }
    
    @Test
    void sendBulkEmails_withEmptyList_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> client.sendBulkEmails(List.of()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Requests list cannot be null or empty");
    }
    
    @Test
    void sendBulkEmails_withNullList_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> client.sendBulkEmails(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Requests list cannot be null or empty");
    }
    
    @Test
    void sendBulkEmails_withInvalidRequestInList_throwsIllegalArgumentException() {
        List<SendEmailRequest> requests = List.of(
            SendEmailRequest.builder()
                .templateKey("welcome-email")
                .recipient("john@example.com")
                .data(Map.of("name", "John Doe"))
                .build(),
            SendEmailRequest.builder()
                .templateKey("welcome-email")
                .recipient("invalid-email") // Invalid email
                .data(Map.of("name", "Jane Doe"))
                .build()
        );
        
        assertThatThrownBy(() -> client.sendBulkEmails(requests))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Validation failed for request 1");
    }
    
    @Test
    void healthCheck_withSuccessResponse_returnsHealthResponse() throws Exception {
        // Arrange
        HealthResponse expectedResponse = new HealthResponse(
            "healthy",
            Instant.now(),
            "1.0.0"
        );
        
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody(objectMapper.writeValueAsString(expectedResponse))
            .addHeader("Content-Type", "application/json"));
        
        // Act
        HealthResponse response = client.healthCheck();
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("healthy");
        assertThat(response.getVersion()).isEqualTo("1.0.0");
        
        // Verify request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getPath()).isEqualTo("/api/v1/sdk/health");
    }
    
    @Test
    void sendEmail_withNetworkError_throwsNetworkException() {
        // Arrange
        mockWebServer.shutdown(); // Simulate network failure
        
        SendEmailRequest request = SendEmailRequest.builder()
            .templateKey("welcome-email")
            .recipient("john@example.com")
            .data(Map.of("name", "John Doe"))
            .build();
        
        // Act & Assert
        assertThatThrownBy(() -> client.sendEmail(request))
            .isInstanceOf(NetworkException.class)
            .hasMessageContaining("Network error occurred");
    }
    
    @Test
    void sendEmail_withMalformedJsonResponse_throwsHuefyException() throws Exception {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("invalid json")
            .addHeader("Content-Type", "application/json"));
        
        SendEmailRequest request = SendEmailRequest.builder()
            .templateKey("welcome-email")
            .recipient("john@example.com")
            .data(Map.of("name", "John Doe"))
            .build();
        
        // Act & Assert
        assertThatThrownBy(() -> client.sendEmail(request))
            .isInstanceOf(HuefyException.class)
            .hasMessageContaining("Failed to parse response");
    }
}