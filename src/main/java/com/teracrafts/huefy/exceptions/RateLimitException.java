package com.teracrafts.huefy.exceptions;

/**
 * Exception thrown when API rate limits are exceeded.
 * 
 * <p>This exception is thrown when the client has exceeded the
 * allowed number of requests per time period. It includes information
 * about when the client can retry the request.</p>
 * 
 * @author Huefy Team
 * @since 1.0.0
 */
public class RateLimitException extends HuefyException {
    private static final long serialVersionUID = 1L;
    
    private final Integer retryAfter;
    
    /**
     * Creates a new RateLimitException with the specified message.
     * 
     * @param message the error message
     */
    public RateLimitException(String message) {
        this(message, null);
    }
    
    /**
     * Creates a new RateLimitException with the specified message and retry delay.
     * 
     * @param message the error message
     * @param retryAfter the number of seconds to wait before retrying
     */
    public RateLimitException(String message, Integer retryAfter) {
        super(message);
        this.retryAfter = retryAfter;
    }
    
    /**
     * Creates a new RateLimitException with the specified message, retry delay, and cause.
     * 
     * @param message the error message
     * @param retryAfter the number of seconds to wait before retrying
     * @param cause the underlying cause
     */
    public RateLimitException(String message, Integer retryAfter, Throwable cause) {
        super(message, cause);
        this.retryAfter = retryAfter;
    }
    
    /**
     * Returns the number of seconds to wait before retrying the request.
     * 
     * @return the retry delay in seconds, or null if not available
     */
    public Integer getRetryAfter() {
        return retryAfter;
    }
}