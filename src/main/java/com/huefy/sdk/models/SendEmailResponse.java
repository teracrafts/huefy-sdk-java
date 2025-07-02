package dev.huefy.sdk.models;

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
    @JsonProperty("messageId")
    private String messageId;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("provider")
    private EmailProvider provider;
    
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    /**
     * Default constructor for JSON deserialization.
     */
    public SendEmailResponse() {
    }
    
    /**
     * Constructor for creating a SendEmailResponse.
     * 
     * @param messageId the unique message ID
     * @param status the email status
     * @param provider the email provider used
     * @param timestamp the timestamp when the email was sent
     */
    public SendEmailResponse(String messageId, String status, EmailProvider provider, Instant timestamp) {
        this.messageId = messageId;
        this.status = status;
        this.provider = provider;
        this.timestamp = timestamp;
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
     * Returns the status of the email.
     * 
     * @return the email status (e.g., "sent", "queued")
     */
    public String getStatus() {
        return status;
    }
    
    /**
     * Sets the email status.
     * 
     * @param status the email status
     */
    public void setStatus(String status) {
        this.status = status;
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
    
    /**
     * Returns the timestamp when the email was sent.
     * 
     * @return the timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }
    
    /**
     * Sets the timestamp.
     * 
     * @param timestamp the timestamp
     */
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "SendEmailResponse{" +
                "messageId='" + messageId + '\'' +
                ", status='" + status + '\'' +
                ", provider=" + provider +
                ", timestamp=" + timestamp +
                '}';
    }
}