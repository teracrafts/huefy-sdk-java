package dev.huefy.sdk.exceptions;

/**
 * Exception thrown when API requests timeout.
 * 
 * <p>This exception is thrown when a request to the Huefy API
 * takes longer than the configured timeout period to complete.</p>
 * 
 * @author Huefy Team
 * @since 1.0.0
 */
public class TimeoutException extends HuefyException {
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new TimeoutException with the specified message.
     * 
     * @param message the error message
     */
    public TimeoutException(String message) {
        super(message);
    }
    
    /**
     * Creates a new TimeoutException with the specified message and cause.
     * 
     * @param message the error message
     * @param cause the underlying cause
     */
    public TimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Creates a new TimeoutException with the specified cause.
     * 
     * @param cause the underlying cause
     */
    public TimeoutException(Throwable cause) {
        super(cause);
    }
}