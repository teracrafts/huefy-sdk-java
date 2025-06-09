package com.huefy.sdk.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Request object for sending multiple emails in a single operation.
 * 
 * <p>This class represents a request to send multiple emails using the bulk
 * email endpoint. It contains a list of individual email requests that will
 * be processed together.</p>
 * 
 * @author Huefy Team
 * @since 1.0.0
 */
public class BulkEmailRequest {
    @JsonProperty("emails")
    private final List<SendEmailRequest> emails;
    
    /**
     * Creates a new BulkEmailRequest with the specified email requests.
     * 
     * @param emails the list of email requests
     */
    public BulkEmailRequest(List<SendEmailRequest> emails) {
        this.emails = emails;
    }
    
    /**
     * Returns the list of email requests.
     * 
     * @return the email requests
     */
    public List<SendEmailRequest> getEmails() {
        return emails;
    }
}