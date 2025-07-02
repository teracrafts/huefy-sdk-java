package dev.huefy.sdk;

import java.time.Duration;

/**
 * Configuration for retry behavior in the Huefy client.
 * 
 * <p>This class defines how the client should handle retries for failed requests,
 * including the maximum number of retries, delays between attempts, and exponential backoff.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * RetryConfig retryConfig = RetryConfig.builder()
 *     .enabled(true)
 *     .maxRetries(3)
 *     .baseDelay(Duration.ofMillis(500))
 *     .maxDelay(Duration.ofSeconds(10))
 *     .multiplier(2.0)
 *     .build();
 * }</pre>
 * 
 * @author Huefy Team
 * @since 1.0.0
 */
public class RetryConfig {
    private final boolean enabled;
    private final int maxRetries;
    private final Duration baseDelay;
    private final Duration maxDelay;
    private final double multiplier;
    
    private RetryConfig(Builder builder) {
        this.enabled = builder.enabled;
        this.maxRetries = builder.maxRetries;
        this.baseDelay = builder.baseDelay;
        this.maxDelay = builder.maxDelay;
        this.multiplier = builder.multiplier;
    }
    
    /**
     * Returns whether retries are enabled.
     * 
     * @return true if retries are enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Returns the maximum number of retry attempts.
     * 
     * @return the maximum number of retries
     */
    public int getMaxRetries() {
        return maxRetries;
    }
    
    /**
     * Returns the base delay between retry attempts.
     * 
     * @return the base delay
     */
    public Duration getBaseDelay() {
        return baseDelay;
    }
    
    /**
     * Returns the maximum delay between retry attempts.
     * 
     * @return the maximum delay
     */
    public Duration getMaxDelay() {
        return maxDelay;
    }
    
    /**
     * Returns the multiplier for exponential backoff.
     * 
     * @return the multiplier
     */
    public double getMultiplier() {
        return multiplier;
    }
    
    /**
     * Creates a new builder for RetryConfig.
     * 
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Creates a disabled retry configuration.
     * 
     * @return a retry configuration with retries disabled
     */
    public static RetryConfig disabled() {
        return builder().enabled(false).build();
    }
    
    /**
     * Builder class for creating RetryConfig instances.
     */
    public static class Builder {
        private boolean enabled = true;
        private int maxRetries = 3;
        private Duration baseDelay = Duration.ofSeconds(1);
        private Duration maxDelay = Duration.ofSeconds(30);
        private double multiplier = 2.0;
        
        /**
         * Sets whether retries are enabled.
         * 
         * @param enabled true to enable retries, false to disable
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }
        
        /**
         * Sets the maximum number of retry attempts.
         * 
         * @param maxRetries the maximum number of retries (must be >= 0)
         * @return this builder
         * @throws IllegalArgumentException if maxRetries is negative
         */
        public Builder maxRetries(int maxRetries) {
            if (maxRetries < 0) {
                throw new IllegalArgumentException("maxRetries must be >= 0");
            }
            this.maxRetries = maxRetries;
            return this;
        }
        
        /**
         * Sets the base delay between retry attempts.
         * 
         * @param baseDelay the base delay (must be positive)
         * @return this builder
         * @throws IllegalArgumentException if baseDelay is null or not positive
         */
        public Builder baseDelay(Duration baseDelay) {
            if (baseDelay == null || baseDelay.isNegative() || baseDelay.isZero()) {
                throw new IllegalArgumentException("baseDelay must be positive");
            }
            this.baseDelay = baseDelay;
            return this;
        }
        
        /**
         * Sets the maximum delay between retry attempts.
         * 
         * @param maxDelay the maximum delay (must be positive)
         * @return this builder
         * @throws IllegalArgumentException if maxDelay is null or not positive
         */
        public Builder maxDelay(Duration maxDelay) {
            if (maxDelay == null || maxDelay.isNegative() || maxDelay.isZero()) {
                throw new IllegalArgumentException("maxDelay must be positive");
            }
            this.maxDelay = maxDelay;
            return this;
        }
        
        /**
         * Sets the multiplier for exponential backoff.
         * 
         * @param multiplier the multiplier (must be >= 1.0)
         * @return this builder
         * @throws IllegalArgumentException if multiplier is less than 1.0
         */
        public Builder multiplier(double multiplier) {
            if (multiplier < 1.0) {
                throw new IllegalArgumentException("multiplier must be >= 1.0");
            }
            this.multiplier = multiplier;
            return this;
        }
        
        /**
         * Builds the RetryConfig instance.
         * 
         * @return the configured RetryConfig
         * @throws IllegalArgumentException if the configuration is invalid
         */
        public RetryConfig build() {
            if (baseDelay.compareTo(maxDelay) > 0) {
                throw new IllegalArgumentException("baseDelay cannot be greater than maxDelay");
            }
            return new RetryConfig(this);
        }
    }
}