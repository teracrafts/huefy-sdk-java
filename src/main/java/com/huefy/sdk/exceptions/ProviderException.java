package dev.huefy.sdk.exceptions;

/**
 * Exception thrown when an email provider rejects or fails to send an email.
 * 
 * <p>This exception is thrown when the underlying email provider
 * (SES, SendGrid, Mailgun, etc.) encounters an error or rejects
 * the email for provider-specific reasons.</p>
 * 
 * @author Huefy Team
 * @since 1.0.0
 */
public class ProviderException extends HuefyException {
    private static final long serialVersionUID = 1L;
    
    private final String provider;
    private final String providerCode;
    
    /**
     * Creates a new ProviderException with the specified message.
     * 
     * @param message the error message
     */
    public ProviderException(String message) {
        this(message, null, null);
    }
    
    /**
     * Creates a new ProviderException with the specified message and provider information.
     * 
     * @param message the error message
     * @param provider the email provider that failed
     * @param providerCode the provider-specific error code
     */
    public ProviderException(String message, String provider, String providerCode) {
        super(message);
        this.provider = provider;
        this.providerCode = providerCode;
    }
    
    /**
     * Creates a new ProviderException with the specified message, provider information, and cause.
     * 
     * @param message the error message
     * @param provider the email provider that failed
     * @param providerCode the provider-specific error code
     * @param cause the underlying cause
     */
    public ProviderException(String message, String provider, String providerCode, Throwable cause) {
        super(message, cause);
        this.provider = provider;
        this.providerCode = providerCode;
    }
    
    /**
     * Returns the email provider that encountered the error.
     * 
     * @return the provider name, or null if not available
     */
    public String getProvider() {
        return provider;
    }
    
    /**
     * Returns the provider-specific error code.
     * 
     * @return the provider error code, or null if not available
     */
    public String getProviderCode() {
        return providerCode;
    }
}