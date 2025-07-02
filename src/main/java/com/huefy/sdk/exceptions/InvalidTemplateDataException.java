package dev.huefy.sdk.exceptions;

import java.util.List;

/**
 * Exception thrown when template data validation fails.
 * 
 * <p>This exception is thrown when the data provided for a template
 * doesn't match the template's requirements, such as missing required
 * fields or invalid data types.</p>
 * 
 * @author Huefy Team
 * @since 1.0.0
 */
public class InvalidTemplateDataException extends HuefyException {
    private static final long serialVersionUID = 1L;
    
    private final List<String> validationErrors;
    
    /**
     * Creates a new InvalidTemplateDataException with the specified message.
     * 
     * @param message the error message
     */
    public InvalidTemplateDataException(String message) {
        this(message, null);
    }
    
    /**
     * Creates a new InvalidTemplateDataException with the specified message and validation errors.
     * 
     * @param message the error message
     * @param validationErrors the list of validation errors
     */
    public InvalidTemplateDataException(String message, List<String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }
    
    /**
     * Creates a new InvalidTemplateDataException with the specified message, validation errors, and cause.
     * 
     * @param message the error message
     * @param validationErrors the list of validation errors
     * @param cause the underlying cause
     */
    public InvalidTemplateDataException(String message, List<String> validationErrors, Throwable cause) {
        super(message, cause);
        this.validationErrors = validationErrors;
    }
    
    /**
     * Returns the list of validation errors.
     * 
     * @return the validation errors, or null if not available
     */
    public List<String> getValidationErrors() {
        return validationErrors;
    }
}