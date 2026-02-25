package com.huefy.http;

import com.huefy.config.HuefyConfig;
import com.huefy.errors.HuefyException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Retry handler implementing exponential backoff with jitter.
 *
 * <p>Automatically retries recoverable errors up to the configured maximum,
 * with increasing delays between attempts to avoid thundering herd problems.</p>
 */
public class RetryHandler {

    private static final Logger logger = LoggerFactory.getLogger(RetryHandler.class);

    private final int maxRetries;
    private final long baseDelay;
    private final long maxDelay;

    /**
     * Creates a retry handler with the given configuration.
     *
     * @param config the retry configuration
     */
    public RetryHandler(HuefyConfig.RetryConfig config) {
        this.maxRetries = config.getMaxRetries();
        this.baseDelay = config.getBaseDelay();
        this.maxDelay = config.getMaxDelay();
    }

    /**
     * Executes an operation with retry logic.
     *
     * <p>If the operation throws a recoverable {@link HuefyException},
     * it will be retried up to {@code maxRetries} times with exponential
     * backoff and jitter between attempts.</p>
     *
     * @param operation the operation to execute
     * @param <T>       the return type
     * @return the result of the operation
     * @throws HuefyException if all retries are exhausted or a non-recoverable error occurs
     */
    public <T> T execute(RetryableOperation<T> operation) {
        HuefyException lastException = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return operation.execute();
            } catch (HuefyException e) {
                lastException = e;

                if (!e.isRecoverable()) {
                    logger.debug("Non-recoverable error, not retrying: {}", e.getMessage());
                    throw e;
                }

                if (attempt >= maxRetries) {
                    logger.warn("All {} retries exhausted", maxRetries);
                    break;
                }

                long delay = calculateDelay(attempt, e.getRetryAfter());
                logger.info("Attempt {}/{} failed ({}), retrying in {}ms",
                        attempt + 1, maxRetries + 1, e.getCode(), delay);

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new HuefyException(
                            "Retry interrupted",
                            com.huefy.errors.ErrorCode.UNKNOWN_ERROR,
                            null,
                            false,
                            null,
                            null,
                            ie
                    );
                }
            }
        }

        throw lastException;
    }

    /**
     * Calculates the delay for the next retry attempt using exponential backoff with jitter.
     *
     * @param attempt    the current attempt number (0-based)
     * @param retryAfter server-suggested retry-after in milliseconds (may be null)
     * @return the delay in milliseconds
     */
    long calculateDelay(int attempt, Long retryAfter) {
        // Honor server-suggested retry-after if available
        if (retryAfter != null && retryAfter > 0) {
            return Math.min(retryAfter, maxDelay);
        }

        // Exponential backoff: baseDelay * 2^attempt
        long exponentialDelay = baseDelay * (1L << attempt);
        long cappedDelay = Math.min(exponentialDelay, maxDelay);

        // Add jitter: random value between 0 and cappedDelay
        long jitter = ThreadLocalRandom.current().nextLong(0, Math.max(1, cappedDelay / 2));
        return cappedDelay + jitter;
    }

    /**
     * Functional interface for retryable operations.
     *
     * @param <T> the return type
     */
    @FunctionalInterface
    public interface RetryableOperation<T> {

        /**
         * Executes the operation.
         *
         * @return the result
         * @throws HuefyException if the operation fails
         */
        T execute() throws HuefyException;
    }
}
