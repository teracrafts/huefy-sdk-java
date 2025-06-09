package com.huefy.sdk.exceptions;

/**
 * Exception thrown when network-related errors occur.
 * 
 * <p>This exception is thrown when there are connectivity issues,
 * DNS resolution failures, or other network-related problems that
 * prevent communication with the Huefy API.</p>
 * 
 * @author Huefy Team
 * @since 1.0.0
 */
public class NetworkException extends HuefyException {
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new NetworkException with the specified message.
     * 
     * @param message the error message
     */
    public NetworkException(String message) {
        super(message);
    }
    
    /**
     * Creates a new NetworkException with the specified message and cause.
     * 
     * @param message the error message
     * @param cause the underlying cause
     */
    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Creates a new NetworkException with the specified cause.
     * 
     * @param cause the underlying cause
     */
    public NetworkException(Throwable cause) {
        super(cause);
    }
}