package com.teracrafts.huefy;

import java.time.Duration;

/**
 * Configuration class for the Huefy client.
 * 
 * <p>This class provides configuration options for customizing the behavior
 * of the HuefyClient, including timeouts, retry settings, and API endpoint.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * HuefyClientConfig config = HuefyClientConfig.builder()
 *     .baseUrl("https://api.huefy.dev")
 *     .connectTimeout(Duration.ofSeconds(10))
 *     .readTimeout(Duration.ofSeconds(30))
 *     .retryConfig(RetryConfig.builder()
 *         .maxRetries(5)
 *         .baseDelay(Duration.ofMillis(500))
 *         .build())
 *     .build();
 * 
 * HuefyClient client = new HuefyClient("api-key", config);
 * }</pre>
 * 
 * @author Huefy Team
 * @since 1.0.0
 */
public class HuefyClientConfig {
    // Production and local endpoints
    public static final String PRODUCTION_HTTP_ENDPOINT = "https://api.huefy.dev/api/v1/sdk";
    public static final String LOCAL_HTTP_ENDPOINT = "http://localhost:8080/api/v1/sdk";

    private final String baseUrl;
    private final Duration connectTimeout;
    private final Duration readTimeout;
    private final Duration writeTimeout;
    private final RetryConfig retryConfig;
    private final boolean local;

    private HuefyClientConfig(Builder builder) {
        this.baseUrl = builder.baseUrl;
        this.connectTimeout = builder.connectTimeout;
        this.readTimeout = builder.readTimeout;
        this.writeTimeout = builder.writeTimeout;
        this.retryConfig = builder.retryConfig;
        this.local = builder.local;
    }

    /**
     * Returns the HTTP endpoint based on configuration.
     *
     * @return the HTTP endpoint URL
     */
    public String getHttpEndpoint() {
        if (baseUrl != null && !baseUrl.isEmpty()) {
            return baseUrl;
        }
        return local ? LOCAL_HTTP_ENDPOINT : PRODUCTION_HTTP_ENDPOINT;
    }

    /**
     * Returns whether local development endpoints are being used.
     *
     * @return true if using local endpoints
     */
    public boolean isLocal() {
        return local;
    }
    
    /**
     * Returns the connection timeout.
     * 
     * @return the connection timeout
     */
    public Duration getConnectTimeout() {
        return connectTimeout;
    }
    
    /**
     * Returns the read timeout.
     * 
     * @return the read timeout
     */
    public Duration getReadTimeout() {
        return readTimeout;
    }
    
    /**
     * Returns the write timeout.
     * 
     * @return the write timeout
     */
    public Duration getWriteTimeout() {
        return writeTimeout;
    }
    
    /**
     * Returns the retry configuration.
     * 
     * @return the retry configuration
     */
    public RetryConfig getRetryConfig() {
        return retryConfig;
    }
    
    /**
     * Creates a new builder for HuefyClientConfig.
     * 
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder class for creating HuefyClientConfig instances.
     */
    public static class Builder {
        private String baseUrl = null;
        private Duration connectTimeout = Duration.ofSeconds(10);
        private Duration readTimeout = Duration.ofSeconds(30);
        private Duration writeTimeout = Duration.ofSeconds(30);
        private RetryConfig retryConfig = RetryConfig.builder().build();
        private boolean local = false;

        /**
         * Sets a custom base URL (overrides local setting).
         *
         * @param baseUrl the base URL
         * @return this builder
         */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * Sets whether to use local development endpoints.
         *
         * @param local true to use local endpoints
         * @return this builder
         */
        public Builder local(boolean local) {
            this.local = local;
            return this;
        }
        
        /**
         * Sets the connection timeout.
         * 
         * @param connectTimeout the connection timeout
         * @return this builder
         */
        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }
        
        /**
         * Sets the read timeout.
         * 
         * @param readTimeout the read timeout
         * @return this builder
         */
        public Builder readTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }
        
        /**
         * Sets the write timeout.
         * 
         * @param writeTimeout the write timeout
         * @return this builder
         */
        public Builder writeTimeout(Duration writeTimeout) {
            this.writeTimeout = writeTimeout;
            return this;
        }
        
        /**
         * Sets the retry configuration.
         * 
         * @param retryConfig the retry configuration
         * @return this builder
         */
        public Builder retryConfig(RetryConfig retryConfig) {
            this.retryConfig = retryConfig;
            return this;
        }
        
        /**
         * Builds the HuefyClientConfig instance.
         * 
         * @return the configured HuefyClientConfig
         */
        public HuefyClientConfig build() {
            return new HuefyClientConfig(this);
        }
    }
}