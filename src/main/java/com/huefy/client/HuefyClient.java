package com.huefy.client;

import com.huefy.config.HuefyConfig;
import com.huefy.errors.HuefyException;
import com.huefy.errors.ErrorCode;
import com.huefy.http.HttpClient;
import com.huefy.utils.Version;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * Main client for the Huefy SDK.
 *
 * <p>Provides methods to interact with the Huefy API.
 * Use the {@link Builder} for advanced configuration or the
 * simple constructor for quick setup.</p>
 *
 * <pre>{@code
 * // Simple usage
 * var client = new HuefyClient("your-api-key");
 *
 * // Advanced usage
 * var client = HuefyClient.builder()
 *     .apiKey("your-api-key")
 *     .baseUrl("https://api.huefy.dev/api/v1/sdk")
 *     .timeout(60000)
 *     .build();
 * }</pre>
 */
public class HuefyClient implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(HuefyClient.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final HuefyConfig config;
    private final HttpClient httpClient;
    private volatile boolean closed = false;

    /**
     * Creates a new client with the given API key and default configuration.
     *
     * @param apiKey the API key for authentication
     * @throws IllegalArgumentException if apiKey is null or blank
     */
    public HuefyClient(String apiKey) {
        this(HuefyConfig.builder()
                .apiKey(apiKey)
                .build());
    }

    /**
     * Creates a new client with the given configuration.
     *
     * @param config the SDK configuration
     * @throws IllegalArgumentException if config is null
     */
    public HuefyClient(HuefyConfig config) {
        Objects.requireNonNull(config, "Config must not be null");
        this.config = config;
        this.httpClient = new HttpClient(config);
        logger.info("Huefy SDK v{} initialized", Version.SDK_VERSION);
    }

    /**
     * Creates a new {@link Builder} for advanced client configuration.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Performs a health check against the API.
     *
     * @return the health check response
     * @throws HuefyException if the request fails
     */
    public HealthResponse healthCheck() {
        ensureOpen();
        try {
            String response = httpClient.request("GET", "/health", null);
            JsonNode node = objectMapper.readTree(response);

            return new HealthResponse(
                    node.has("status") ? node.get("status").asText() : "unknown",
                    node.has("version") ? node.get("version").asText() : null,
                    node.has("timestamp") ? node.get("timestamp").asText() : null
            );
        } catch (HuefyException e) {
            throw e;
        } catch (Exception e) {
            throw HuefyException.networkError("Health check failed: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the current SDK configuration.
     *
     * @return the configuration
     */
    public HuefyConfig getConfig() {
        return config;
    }

    /**
     * Returns whether this client has been closed.
     *
     * @return true if closed
     */
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            httpClient.close();
            logger.info("Huefy SDK client closed");
        }
    }

    private void ensureOpen() {
        if (closed) {
            throw new HuefyException(
                    "Client has been closed",
                    ErrorCode.UNKNOWN_ERROR,
                    null,
                    false
            );
        }
    }

    /**
     * Health check response record.
     */
    public record HealthResponse(String status, String version, String timestamp) {

        public boolean isHealthy() {
            return "ok".equalsIgnoreCase(status) || "healthy".equalsIgnoreCase(status);
        }
    }

    /**
     * Builder for creating {@link HuefyClient} instances with advanced configuration.
     */
    public static final class Builder {

        private final HuefyConfig.Builder configBuilder = HuefyConfig.builder();

        private Builder() {}

        public Builder apiKey(String apiKey) {
            configBuilder.apiKey(apiKey);
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            configBuilder.baseUrl(baseUrl);
            return this;
        }

        public Builder timeout(long timeout) {
            configBuilder.timeout(timeout);
            return this;
        }

        public Builder secondaryApiKey(String secondaryApiKey) {
            configBuilder.secondaryApiKey(secondaryApiKey);
            return this;
        }

        public Builder enableRequestSigning(boolean enable) {
            configBuilder.enableRequestSigning(enable);
            return this;
        }

        public Builder enableErrorSanitization(boolean enable) {
            configBuilder.enableErrorSanitization(enable);
            return this;
        }

        public Builder retryConfig(HuefyConfig.RetryConfig retryConfig) {
            configBuilder.retryConfig(retryConfig);
            return this;
        }

        public Builder circuitBreakerConfig(HuefyConfig.CircuitBreakerConfig circuitBreakerConfig) {
            configBuilder.circuitBreakerConfig(circuitBreakerConfig);
            return this;
        }

        /**
         * Builds the client.
         *
         * @return a new {@link HuefyClient} instance
         */
        public HuefyClient build() {
            return new HuefyClient(configBuilder.build());
        }
    }
}
