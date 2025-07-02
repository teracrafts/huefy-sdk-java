package dev.huefy.sdk.exceptions;

/**
 * Base exception class for all Huefy SDK exceptions.
 * 
 * <p>This is the parent class for all exceptions thrown by the Huefy SDK.
 * It provides a common interface for handling errors and includes support
 * for error chaining and detailed error information.</p>
 * 
 * @author Huefy Team
 * @since 1.0.0
 */
public class HuefyException extends Exception {
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new HuefyException with the specified message.
     * 
     * @param message the error message
     */
    public HuefyException(String message) {
        super(message);
    }
    
    /**
     * Creates a new HuefyException with the specified message and cause.
     * 
     * @param message the error message
     * @param cause the underlying cause
     */
    public HuefyException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Creates a new HuefyException with the specified cause.
     * 
     * @param cause the underlying cause
     */
    public HuefyException(Throwable cause) {
        super(cause);
    }
}