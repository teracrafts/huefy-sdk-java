package com.teracrafts.huefy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.teracrafts.huefy.exceptions.*;
import com.teracrafts.huefy.models.*;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Main client for the Huefy email sending platform.
 * 
 * <p>The HuefyClient provides a simple interface for sending template-based emails
 * through the Huefy API with support for multiple email providers, retry logic,
 * and comprehensive error handling.</p>
 * 
 * <p>Basic usage:</p>
 * <pre>{@code
 * HuefyClient client = new HuefyClient("your-api-key");
 * 
 * SendEmailRequest request = SendEmailRequest.builder()
 *     .templateKey("welcome-email")
 *     .recipient("john@example.com")
 *     .data(Map.of("name", "John Doe", "company", "Acme Corp"))
 *     .build();
 * 
 * SendEmailResponse response = client.sendEmail(request);
 * System.out.println("Email sent: " + response.getMessageId());
 * }</pre>
 * 
 * @author Huefy Team
 * @since 1.0.0
 */
public class HuefyClient {
    private static final Logger logger = LoggerFactory.getLogger(HuefyClient.class);
    
    private static final String USER_AGENT = "Huefy-Java-SDK/2.1.2";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final String apiKey;
    private final String baseUrl;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final RetryConfig retryConfig;
    
    /**
     * Creates a new HuefyClient with the specified API key and default configuration.
     * 
     * @param apiKey the Huefy API key
     * @throws IllegalArgumentException if apiKey is null or empty
     */
    public HuefyClient(String apiKey) {
        this(apiKey, HuefyClientConfig.builder().build());
    }
    
    /**
     * Creates a new HuefyClient with the specified API key and configuration.
     * 
     * @param apiKey the Huefy API key
     * @param config the client configuration
     * @throws IllegalArgumentException if apiKey is null or empty
     */
    public HuefyClient(String apiKey, HuefyClientConfig config) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API key cannot be null or empty");
        }
        
        this.apiKey = apiKey.trim();
        this.baseUrl = config.getHttpEndpoint();
        this.retryConfig = config.getRetryConfig();
        
        // Configure ObjectMapper
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        
        // Configure HTTP client
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
            .connectTimeout(config.getConnectTimeout())
            .readTimeout(config.getReadTimeout())
            .writeTimeout(config.getWriteTimeout())
            .addInterceptor(new UserAgentInterceptor())
            .addInterceptor(new AuthenticationInterceptor(apiKey));
        
        if (config.getRetryConfig().isEnabled()) {
            httpClientBuilder.addInterceptor(new RetryInterceptor(retryConfig));
        }
        
        this.httpClient = httpClientBuilder.build();
        
        logger.debug("HuefyClient initialized with base URL: {}", this.baseUrl);
    }
    
    /**
     * Sends a single email using a template.
     * 
     * @param request the email request
     * @return the email response
     * @throws HuefyException if the request fails
     * @throws IllegalArgumentException if request is null or invalid
     */
    public SendEmailResponse sendEmail(SendEmailRequest request) throws HuefyException {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        
        request.validate();
        return makeRequest("POST", "/emails/send", request, SendEmailResponse.class);
    }
    
    /**
     * Sends multiple emails in a single request.
     * 
     * @param requests the list of email requests
     * @return the bulk email response
     * @throws HuefyException if the request fails
     * @throws IllegalArgumentException if requests is null, empty, or contains invalid requests
     */
    public BulkEmailResponse sendBulkEmails(List<SendEmailRequest> requests) throws HuefyException {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("Requests list cannot be null or empty");
        }
        
        // Validate all requests
        for (int i = 0; i < requests.size(); i++) {
            try {
                requests.get(i).validate();
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Validation failed for request " + i + ": " + e.getMessage(), e);
            }
        }
        
        BulkEmailRequest bulkRequest = new BulkEmailRequest(requests);
        return makeRequest("POST", "/emails/bulk", bulkRequest, BulkEmailResponse.class);
    }
    
    /**
     * Checks the health status of the Huefy API.
     * 
     * @return the health response
     * @throws HuefyException if the request fails
     */
    public HealthResponse healthCheck() throws HuefyException {
        return makeRequest("GET", "/health", null, HealthResponse.class);
    }
    
    /**
     * Handles HTTP responses and converts them to appropriate objects or exceptions.
     * 
     * @param response the HTTP response
     * @param responseClass the expected response class
     * @param <T> the response type
     * @return the deserialized response object
     * @throws HuefyException if the response indicates an error
     */
    /**
     * Makes an HTTP request to the Huefy API.
     */
    private <T> T makeRequest(String method, String endpoint, Object data, Class<T> responseClass) throws HuefyException {
        try {
            // Build URL
            String cleanBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
            // Only add /api/v1/sdk if baseUrl doesn't already include it
            String requestUrl = cleanBaseUrl.contains("/api/v1/sdk")
                ? cleanBaseUrl + endpoint
                : cleanBaseUrl + "/api/v1/sdk" + endpoint;

            Request.Builder requestBuilder = new Request.Builder().url(requestUrl);

            if ("POST".equals(method) && data != null) {
                String jsonBody = objectMapper.writeValueAsString(data);
                RequestBody body = RequestBody.create(jsonBody, JSON);
                requestBuilder.post(body);
            } else if ("GET".equals(method)) {
                requestBuilder.get();
            }

            Request httpRequest = requestBuilder.build();

            try (Response response = httpClient.newCall(httpRequest).execute()) {
                return handleResponse(response, responseClass);
            }
        } catch (JsonProcessingException e) {
            throw new HuefyException("Failed to serialize request", e);
        } catch (IOException e) {
            throw new NetworkException("Network error occurred", e);
        }
    }
    
    private <T> T handleResponse(Response response, Class<T> responseClass) throws HuefyException {
        try {
            String responseBody = response.body() != null ? response.body().string() : "";
            
            if (response.isSuccessful()) {
                return objectMapper.readValue(responseBody, responseClass);
            } else {
                // Parse error response
                ErrorResponse errorResponse;
                try {
                    errorResponse = objectMapper.readValue(responseBody, ErrorResponse.class);
                } catch (JsonProcessingException e) {
                    // Fallback to generic error
                    ErrorDetail errorDetail = new ErrorDetail(
                        "HTTP_" + response.code(),
                        responseBody.isEmpty() ? "HTTP " + response.code() : responseBody,
                        null
                    );
                    errorResponse = new ErrorResponse(errorDetail);
                }
                
                throw createExceptionFromErrorResponse(errorResponse, response.code());
            }
        } catch (JsonProcessingException e) {
            throw new HuefyException("Failed to parse response", e);
        } catch (IOException e) {
            throw new NetworkException("Failed to read response body", e);
        }
    }
    
    /**
     * Creates appropriate exception types from error responses.
     * 
     * @param errorResponse the error response
     * @param statusCode the HTTP status code
     * @return the appropriate exception
     */
    private HuefyException createExceptionFromErrorResponse(ErrorResponse errorResponse, int statusCode) {
        ErrorDetail error = errorResponse.getError();
        String code = error.getCode();
        String message = error.getMessage();
        
        switch (code) {
            case "AUTHENTICATION_FAILED":
                return new AuthenticationException(message);
            case "TEMPLATE_NOT_FOUND":
                String templateKey = error.getDetails() != null ? 
                    (String) error.getDetails().get("templateKey") : null;
                return new TemplateNotFoundException(message, templateKey);
            case "INVALID_TEMPLATE_DATA":
                @SuppressWarnings("unchecked")
                List<String> validationErrors = error.getDetails() != null ?
                    (List<String>) error.getDetails().get("validationErrors") : null;
                return new InvalidTemplateDataException(message, validationErrors);
            case "INVALID_RECIPIENT":
                return new InvalidRecipientException(message);
            case "PROVIDER_ERROR":
                String provider = error.getDetails() != null ?
                    (String) error.getDetails().get("provider") : null;
                String providerCode = error.getDetails() != null ?
                    (String) error.getDetails().get("providerCode") : null;
                return new ProviderException(message, provider, providerCode);
            case "RATE_LIMIT_EXCEEDED":
                Integer retryAfter = error.getDetails() != null ?
                    (Integer) error.getDetails().get("retryAfter") : null;
                return new RateLimitException(message, retryAfter);
            case "VALIDATION_FAILED":
                return new ValidationException(message);
            case "TIMEOUT":
                return new TimeoutException(message);
            case "NETWORK_ERROR":
                return new NetworkException(message);
            default:
                return new HuefyException(message);
        }
    }
    
    /**
     * Closes the HTTP client and releases resources.
     * This method should be called when the client is no longer needed.
     */
    public void close() {
        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
            httpClient.connectionPool().evictAll();
            if (httpClient.cache() != null) {
                try {
                    httpClient.cache().close();
                } catch (IOException e) {
                    logger.warn("Error closing HTTP cache", e);
                }
            }
        }
    }
    
    /**
     * Interceptor that adds the User-Agent header to all requests.
     */
    private static class UserAgentInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request().newBuilder()
                .header("User-Agent", USER_AGENT)
                .build();
            return chain.proceed(request);
        }
    }
    
    /**
     * Interceptor that adds authentication headers to all requests.
     */
    private static class AuthenticationInterceptor implements Interceptor {
        private final String apiKey;
        
        public AuthenticationInterceptor(String apiKey) {
            this.apiKey = apiKey;
        }
        
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request().newBuilder()
                .header("X-API-Key", apiKey)
                .build();
            return chain.proceed(request);
        }
    }
    
    /**
     * Interceptor that handles automatic retries for failed requests.
     */
    private static class RetryInterceptor implements Interceptor {
        private static final Logger logger = LoggerFactory.getLogger(RetryInterceptor.class);
        private final RetryConfig retryConfig;
        
        public RetryInterceptor(RetryConfig retryConfig) {
            this.retryConfig = retryConfig;
        }
        
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = null;
            IOException lastException = null;
            
            for (int attempt = 0; attempt <= retryConfig.getMaxRetries(); attempt++) {
                if (attempt > 0) {
                    long delay = calculateDelay(attempt - 1);
                    logger.debug("Retrying request after {}ms (attempt {})", delay, attempt);
                    
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Request interrupted", e);
                    }
                }
                
                try {
                    if (response != null) {
                        response.close();
                    }
                    response = chain.proceed(request);
                    
                    if (response.isSuccessful() || !isRetryableStatus(response.code())) {
                        return response;
                    }
                    
                    logger.debug("Request failed with status {} (attempt {})", response.code(), attempt + 1);
                } catch (IOException e) {
                    lastException = e;
                    logger.debug("Request failed with exception (attempt {}): {}", attempt + 1, e.getMessage());
                    
                    if (attempt == retryConfig.getMaxRetries()) {
                        throw e;
                    }
                }
            }
            
            if (response != null) {
                return response;
            } else if (lastException != null) {
                throw lastException;
            } else {
                throw new IOException("Request failed after " + (retryConfig.getMaxRetries() + 1) + " attempts");
            }
        }
        
        private long calculateDelay(int attemptNumber) {
            long delay = (long) (retryConfig.getBaseDelay().toMillis() * Math.pow(retryConfig.getMultiplier(), attemptNumber));
            return Math.min(delay, retryConfig.getMaxDelay().toMillis());
        }
        
        private boolean isRetryableStatus(int statusCode) {
            return statusCode >= 500 || statusCode == 429; // Server errors and rate limiting
        }
    }
}