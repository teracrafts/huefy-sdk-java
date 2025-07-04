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
    private final String proxyUrl;
    private final String baseUrl;
    private final Duration connectTimeout;
    private final Duration readTimeout;
    private final Duration writeTimeout;
    private final RetryConfig retryConfig;
    
    private HuefyClientConfig(Builder builder) {
        this.proxyUrl = builder.proxyUrl;
        this.baseUrl = builder.baseUrl;
        this.connectTimeout = builder.connectTimeout;
        this.readTimeout = builder.readTimeout;
        this.writeTimeout = builder.writeTimeout;
        this.retryConfig = builder.retryConfig;
    }
    
    /**
     * Returns the proxy URL for optimized routing.
     * 
     * @return the proxy URL, or null if not configured
     */
    public String getProxyUrl() {
        return proxyUrl;
    }
    
    /**
     * Returns the base URL for the Huefy API.
     * 
     * @return the base URL
     */
    public String getBaseUrl() {
        return baseUrl;
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
        private String proxyUrl = "http://localhost:8080/kernel-proxy";
        private String baseUrl = "https://api.huefy.dev";
        private Duration connectTimeout = Duration.ofSeconds(10);
        private Duration readTimeout = Duration.ofSeconds(30);
        private Duration writeTimeout = Duration.ofSeconds(30);
        private RetryConfig retryConfig = RetryConfig.builder().build();
        
        /**
         * Sets the proxy URL for optimized routing.
         * 
         * @param proxyUrl the proxy URL
         * @return this builder
         */
        public Builder proxyUrl(String proxyUrl) {
            this.proxyUrl = proxyUrl;
            return this;
        }
        
        /**
         * Sets the base URL for the Huefy API.
         * 
         * @param baseUrl the base URL
         * @return this builder
         */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
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