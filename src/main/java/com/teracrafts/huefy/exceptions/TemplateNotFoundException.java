package com.teracrafts.huefy.exceptions;

/**
 * Exception thrown when a specified email template is not found.
 * 
 * <p>This exception is thrown when attempting to send an email using
 * a template key that doesn't exist in the Huefy system.</p>
 * 
 * @author Huefy Team
 * @since 1.0.0
 */
public class TemplateNotFoundException extends HuefyException {
    private static final long serialVersionUID = 1L;
    
    private final String templateKey;
    
    /**
     * Creates a new TemplateNotFoundException with the specified message.
     * 
     * @param message the error message
     */
    public TemplateNotFoundException(String message) {
        this(message, null);
    }
    
    /**
     * Creates a new TemplateNotFoundException with the specified message and template key.
     * 
     * @param message the error message
     * @param templateKey the template key that was not found
     */
    public TemplateNotFoundException(String message, String templateKey) {
        super(message);
        this.templateKey = templateKey;
    }
    
    /**
     * Creates a new TemplateNotFoundException with the specified message, template key, and cause.
     * 
     * @param message the error message
     * @param templateKey the template key that was not found
     * @param cause the underlying cause
     */
    public TemplateNotFoundException(String message, String templateKey, Throwable cause) {
        super(message, cause);
        this.templateKey = templateKey;
    }
    
    /**
     * Returns the template key that was not found.
     * 
     * @return the template key, or null if not available
     */
    public String getTemplateKey() {
        return templateKey;
    }
}