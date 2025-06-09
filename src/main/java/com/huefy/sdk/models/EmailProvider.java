package com.huefy.sdk.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing supported email providers in the Huefy platform.
 * 
 * <p>This enum defines the email providers that can be used to send emails
 * through the Huefy API. Each provider has its own capabilities and configuration.</p>
 * 
 * @author Huefy Team
 * @since 1.0.0
 */
public enum EmailProvider {
    /**
     * Amazon Simple Email Service (SES)
     */
    SES("ses"),
    
    /**
     * SendGrid email service
     */
    SENDGRID("sendgrid"),
    
    /**
     * Mailgun email service
     */
    MAILGUN("mailgun"),
    
    /**
     * Mailchimp Transactional (formerly Mandrill)
     */
    MAILCHIMP("mailchimp");
    
    private final String value;
    
    EmailProvider(String value) {
        this.value = value;
    }
    
    /**
     * Returns the string value of the email provider.
     * 
     * @return the string value
     */
    @JsonValue
    public String getValue() {
        return value;
    }
    
    /**
     * Creates an EmailProvider from its string value.
     * 
     * @param value the string value
     * @return the corresponding EmailProvider
     * @throws IllegalArgumentException if the value is not a valid provider
     */
    @JsonCreator
    public static EmailProvider fromValue(String value) {
        if (value == null) {
            return null;
        }
        
        for (EmailProvider provider : EmailProvider.values()) {
            if (provider.value.equalsIgnoreCase(value)) {
                return provider;
            }
        }
        
        throw new IllegalArgumentException("Invalid email provider: " + value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}