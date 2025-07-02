package com.teracrafts.huefy.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Request object for sending a single email.
 * 
 * <p>This class represents a request to send an email using a template.
 * It includes the template key, recipient email address, template data,
 * and optional provider specification.</p>
 * 
 * <p>Example usage:</p>
 * <pre>{@code
 * SendEmailRequest request = SendEmailRequest.builder()
 *     .templateKey("welcome-email")
 *     .recipient("john@example.com")
 *     .data(Map.of(
 *         "name", "John Doe",
 *         "company", "Acme Corp"
 *     ))
 *     .provider(EmailProvider.SENDGRID)
 *     .build();
 * }</pre>
 * 
 * @author Huefy Team
 * @since 1.0.0
 */
public class SendEmailRequest {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    @JsonProperty("templateKey")
    private final String templateKey;
    
    @JsonProperty("data")
    private final Map<String, Object> data;
    
    @JsonProperty("recipient")
    private final String recipient;
    
    @JsonProperty("providerType")
    private final EmailProvider provider;
    
    private SendEmailRequest(Builder builder) {
        this.templateKey = builder.templateKey;
        this.data = builder.data;
        this.recipient = builder.recipient;
        this.provider = builder.provider;
    }
    
    /**
     * Returns the template key.
     * 
     * @return the template key
     */
    public String getTemplateKey() {
        return templateKey;
    }
    
    /**
     * Returns the template data.
     * 
     * @return the template data
     */
    public Map<String, Object> getData() {
        return data;
    }
    
    /**
     * Returns the recipient email address.
     * 
     * @return the recipient email address
     */
    public String getRecipient() {
        return recipient;
    }
    
    /**
     * Returns the email provider.
     * 
     * @return the email provider, or null if not specified
     */
    public EmailProvider getProvider() {
        return provider;
    }
    
    /**
     * Validates the request data.
     * 
     * @throws IllegalArgumentException if the request is invalid
     */
    public void validate() {
        if (templateKey == null || templateKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Template key is required");
        }
        
        if (recipient == null || recipient.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient is required");
        }
        
        if (!EMAIL_PATTERN.matcher(recipient.trim()).matches()) {
            throw new IllegalArgumentException("Invalid recipient email address: " + recipient);
        }
        
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Template data is required");
        }
    }
    
    /**
     * Creates a new builder for SendEmailRequest.
     * 
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Builder class for creating SendEmailRequest instances.
     */
    public static class Builder {
        private String templateKey;
        private Map<String, Object> data;
        private String recipient;
        private EmailProvider provider;
        
        /**
         * Sets the template key.
         * 
         * @param templateKey the template key
         * @return this builder
         */
        public Builder templateKey(String templateKey) {
            this.templateKey = templateKey;
            return this;
        }
        
        /**
         * Sets the template data.
         * 
         * @param data the template data
         * @return this builder
         */
        public Builder data(Map<String, Object> data) {
            this.data = data;
            return this;
        }
        
        /**
         * Sets the recipient email address.
         * 
         * @param recipient the recipient email address
         * @return this builder
         */
        public Builder recipient(String recipient) {
            this.recipient = recipient;
            return this;
        }
        
        /**
         * Sets the email provider.
         * 
         * @param provider the email provider
         * @return this builder
         */
        public Builder provider(EmailProvider provider) {
            this.provider = provider;
            return this;
        }
        
        /**
         * Builds the SendEmailRequest instance.
         * 
         * @return the configured SendEmailRequest
         */
        public SendEmailRequest build() {
            return new SendEmailRequest(this);
        }
    }
}