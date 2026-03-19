package com.huefy.http;

import com.huefy.config.HuefyConfig;
import com.huefy.errors.ErrorCode;
import com.huefy.errors.ErrorSanitizer;
import com.huefy.errors.HuefyException;
import com.huefy.security.Security;
import com.huefy.utils.Version;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huefy.config.RateLimitInfo;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * HTTP client for the Huefy SDK.
 *
 * <p>Handles request execution with retry logic, circuit breaking,
 * key rotation on 401 responses, and optional HMAC request signing.</p>
 */
public class HttpClient {

    private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);
    private static final String USER_AGENT = "huefy-java/" + Version.SDK_VERSION;

    private final HuefyConfig config;
    private final java.net.http.HttpClient httpClient;
    private final RetryHandler retryHandler;
    private final CircuitBreaker circuitBreaker;
    private volatile String currentApiKey;
    private final AtomicBoolean rotatedToSecondary = new AtomicBoolean(false);
    private final Object rotationLock = new Object();

    /**
     * Creates a new HTTP client with the given configuration.
     *
     * @param config the SDK configuration
     */
    public HttpClient(HuefyConfig config) {
        this.config = Objects.requireNonNull(config, "Config must not be null");
        this.currentApiKey = config.getApiKey();
        this.httpClient = java.net.http.HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.getTimeout()))
                .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
                .build();
        this.retryHandler = new RetryHandler(config.getRetryConfig());
        this.circuitBreaker = new CircuitBreaker(config.getCircuitBreakerConfig());
    }

    /**
     * Sends an HTTP request with retry and circuit breaker support.
     *
     * @param method the HTTP method (GET, POST, PUT, DELETE)
     * @param path   the request path (appended to base URL)
     * @param body   the request body (may be null for GET/DELETE)
     * @return the response body as a string
     * @throws HuefyException if the request fails after all retries
     */
    public String request(String method, String path, String body) {
        circuitBreaker.ensureClosed();

        return retryHandler.execute(() -> {
            try {
                HttpResponse<String> response = executeRequest(method, path, body);
                int statusCode = response.statusCode();

                if (statusCode >= 200 && statusCode < 300) {
                    circuitBreaker.recordSuccess();
                    parseRateLimitHeaders(response);
                    return response.body();
                }

                // Handle 401 with key rotation — only one thread performs the rotation
                if (statusCode == 401 && !rotatedToSecondary.get() && config.getSecondaryApiKey() != null) {
                    synchronized (rotationLock) {
                        if (!rotatedToSecondary.get()) {
                            logger.warn("Primary API key rejected, rotating to secondary key");
                            currentApiKey = config.getSecondaryApiKey();
                            rotatedToSecondary.set(true);
                        }
                    }

                    // Retry with secondary key
                    HttpResponse<String> retryResponse = executeRequest(method, path, body);
                    if (retryResponse.statusCode() >= 200 && retryResponse.statusCode() < 300) {
                        circuitBreaker.recordSuccess();
                        parseRateLimitHeaders(retryResponse);
                        return retryResponse.body();
                    }
                    statusCode = retryResponse.statusCode();
                    response = retryResponse;
                }

                circuitBreaker.recordFailure();

                String responseBody = response.body();
                String requestId = response.headers()
                        .firstValue("X-Request-Id")
                        .orElse(null);
                String retryAfterHeader = response.headers()
                        .firstValue("Retry-After")
                        .orElse(null);

                if (config.isEnableErrorSanitization() && responseBody != null) {
                    responseBody = ErrorSanitizer.sanitize(responseBody);
                }

                throw HuefyException.fromResponse(statusCode, responseBody, requestId, retryAfterHeader);

            } catch (HuefyException e) {
                throw e;
            } catch (java.net.http.HttpTimeoutException e) {
                circuitBreaker.recordFailure();
                throw new HuefyException(
                        "Request timed out after " + config.getTimeout() + "ms",
                        ErrorCode.TIMEOUT_ERROR,
                        null,
                        true,
                        null,
                        null,
                        e
                );
            } catch (java.net.ConnectException e) {
                circuitBreaker.recordFailure();
                throw new HuefyException(
                        "Connection refused: " + config.getBaseUrl(),
                        ErrorCode.CONNECTION_REFUSED,
                        null,
                        true,
                        null,
                        null,
                        e
                );
            } catch (Exception e) {
                circuitBreaker.recordFailure();
                throw HuefyException.networkError(
                        "Request failed: " + e.getMessage(), e
                );
            }
        });
    }

    /**
     * Closes the underlying HTTP client resources.
     */
    public void close() {
        // java.net.http.HttpClient does not require explicit closing,
        // but this method exists for future resource cleanup
        logger.debug("HTTP client closed");
    }

    private void parseRateLimitHeaders(HttpResponse<?> response) {
        if (config.getOnRateLimitUpdate() == null && config.getOnRateLimitWarning() == null) {
            return;
        }

        String limitHeader = response.headers().firstValue("X-RateLimit-Limit").orElse(null);
        String remainingHeader = response.headers().firstValue("X-RateLimit-Remaining").orElse(null);
        String resetHeader = response.headers().firstValue("X-RateLimit-Reset").orElse(null);

        if (limitHeader == null || remainingHeader == null || resetHeader == null) {
            return;
        }

        try {
            int limit = Integer.parseInt(limitHeader);
            int remaining = Integer.parseInt(remainingHeader);
            Instant resetAt = Instant.ofEpochSecond(Long.parseLong(resetHeader));
            RateLimitInfo info = new RateLimitInfo(limit, remaining, resetAt);

            if (config.getOnRateLimitUpdate() != null) {
                config.getOnRateLimitUpdate().accept(info);
            }

            if (config.getOnRateLimitWarning() != null && limit > 0 && remaining < limit * 0.2) {
                config.getOnRateLimitWarning().accept(info);
            }
        } catch (NumberFormatException e) {
            logger.debug("Failed to parse rate limit headers: {}", e.getMessage());
        }
    }

    private HttpResponse<String> executeRequest(String method, String path, String body)
            throws Exception {

        String url = config.getBaseUrl() + path;
        String timestamp = String.valueOf(System.currentTimeMillis());

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(config.getTimeout()))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent", USER_AGENT)
                .header("X-API-Key", currentApiKey)
                .header("X-SDK-Version", Version.SDK_VERSION)
                .header("X-Timestamp", timestamp);

        // Serialize body once for both signing and sending
        String bodyString = body != null ? body : "";

        // Add HMAC signature if request signing is enabled
        if (config.isEnableRequestSigning()) {
            String message = timestamp + "." + bodyString;
            String signature = Security.generateHmacSignature(message, currentApiKey);
            requestBuilder.header("X-Signature", signature);
            requestBuilder.header("X-Signature-Algorithm", "HMAC-SHA256");
        }

        // Set method and body using the same string used for signing
        HttpRequest.BodyPublisher bodyPublisher = body != null
                ? HttpRequest.BodyPublishers.ofString(bodyString)
                : HttpRequest.BodyPublishers.noBody();

        requestBuilder.method(method.toUpperCase(), bodyPublisher);

        HttpRequest httpRequest = requestBuilder.build();
        logger.debug("{} {}", method.toUpperCase(), url);

        return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    }
}
