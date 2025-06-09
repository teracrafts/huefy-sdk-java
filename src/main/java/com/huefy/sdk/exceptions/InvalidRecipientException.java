package com.huefy.sdk.exceptions;

/**
 * Exception thrown when a recipient email address is invalid.
 * 
 * <p>This exception is thrown when the recipient email address
 * is malformed, empty, or otherwise invalid according to email
 * address validation rules.</p>
 * 
 * @author Huefy Team
 * @since 1.0.0
 */
public class InvalidRecipientException extends HuefyException {
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new InvalidRecipientException with the specified message.
     * 
     * @param message the error message
     */
    public InvalidRecipientException(String message) {
        super(message);
    }
    
    /**
     * Creates a new InvalidRecipientException with the specified message and cause.
     * 
     * @param message the error message
     * @param cause the underlying cause
     */
    public InvalidRecipientException(String message, Throwable cause) {
        super(message, cause);
    }
}