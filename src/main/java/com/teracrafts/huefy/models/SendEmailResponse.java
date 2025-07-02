package com.teracrafts.huefy.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Response object for email sending operations.
 * 
 * <p>This class represents the response from the Huefy API when an email
 * is successfully sent. It contains information about the sent email including
 * the message ID, status, provider used, and timestamp.</p>
 * 
 * @author Huefy Team
 * @since 1.0.0
 */
public class SendEmailResponse {
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("messageId")
    private String messageId;
    
    @JsonProperty("provider")
    private EmailProvider provider;
    
    /**
     * Default constructor for JSON deserialization.
     */
    public SendEmailResponse() {
    }
    
    /**
     * Constructor for creating a SendEmailResponse.
     * 
     * @param success whether the email was sent successfully
     * @param message human-readable status message
     * @param messageId the unique message ID
     * @param provider the email provider used
     */
    public SendEmailResponse(boolean success, String message, String messageId, EmailProvider provider) {
        this.success = success;
        this.message = message;
        this.messageId = messageId;
        this.provider = provider;
    }
    
    /**
     * Returns whether the email was sent successfully.
     * 
     * @return true if the email was sent successfully
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * Sets the success status.
     * 
     * @param success the success status
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    /**
     * Returns the human-readable status message.
     * 
     * @return the status message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Sets the status message.
     * 
     * @param message the status message
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * Returns the unique message ID for the sent email.
     * 
     * @return the message ID
     */
    public String getMessageId() {
        return messageId;
    }
    
    /**
     * Sets the message ID.
     * 
     * @param messageId the message ID
     */
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    
    /**
     * Returns the email provider that was used to send the email.
     * 
     * @return the email provider
     */
    public EmailProvider getProvider() {
        return provider;
    }
    
    /**
     * Sets the email provider.
     * 
     * @param provider the email provider
     */
    public void setProvider(EmailProvider provider) {
        this.provider = provider;
    }
    
    @Override
    public String toString() {
        return "SendEmailResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", messageId='" + messageId + '\'' +
                ", provider=" + provider +
                '}';
    }
}