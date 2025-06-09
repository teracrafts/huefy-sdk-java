package com.huefy.sdk.exceptions;

/**
 * Exception thrown when request validation fails.
 * 
 * <p>This exception is thrown when the request data doesn't meet
 * the API's validation requirements, such as missing required fields
 * or invalid parameter values.</p>
 * 
 * @author Huefy Team
 * @since 1.0.0
 */
public class ValidationException extends HuefyException {
    private static final long serialVersionUID = 1L;
    
    /**
     * Creates a new ValidationException with the specified message.
     * 
     * @param message the error message
     */
    public ValidationException(String message) {
        super(message);
    }
    
    /**
     * Creates a new ValidationException with the specified message and cause.
     * 
     * @param message the error message
     * @param cause the underlying cause
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}