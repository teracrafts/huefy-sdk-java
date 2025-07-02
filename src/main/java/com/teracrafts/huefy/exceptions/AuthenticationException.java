package com.teracrafts.huefy.exceptions;

/**
 * Exception thrown when authentication fails.
 * 
 * <p>This exception is thrown when the API key is invalid, expired,
 * or when there are other authentication-related issues.</p>
 * 
 * @author Huefy Team
 * @since 1.0.0
 */
public class AuthenticationException extends HuefyException {
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new AuthenticationException with the specified message.
     * 
     * @param message the error message
     */
    public AuthenticationException(String message) {
        super(message);
    }
    
    /**
     * Creates a new AuthenticationException with the specified message and cause.
     * 
     * @param message the error message
     * @param cause the underlying cause
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}