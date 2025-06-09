package com.huefy.sdk.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response object for bulk email operations.
 * 
 * <p>This class represents the response from the Huefy API when multiple emails
 * are sent in a single bulk operation. It contains a list of results for each
 * email that was processed.</p>
 * 
 * @author Huefy Team
 * @since 1.0.0
 */
public class BulkEmailResponse {
    @JsonProperty("results")
    private List<BulkEmailResult> results;
    
    /**
     * Default constructor for JSON deserialization.
     */
    public BulkEmailResponse() {
    }
    
    /**
     * Creates a new BulkEmailResponse with the specified results.
     * 
     * @param results the list of email results
     */
    public BulkEmailResponse(List<BulkEmailResult> results) {
        this.results = results;
    }
    
    /**
     * Returns the list of email results.
     * 
     * @return the email results
     */
    public List<BulkEmailResult> getResults() {
        return results;
    }
    
    /**
     * Sets the list of email results.
     * 
     * @param results the email results
     */
    public void setResults(List<BulkEmailResult> results) {
        this.results = results;
    }
    
    /**
     * Represents the result of a single email in a bulk operation.
     */
    public static class BulkEmailResult {
        @JsonProperty("success")
        private boolean success;
        
        @JsonProperty("result")
        private SendEmailResponse result;
        
        @JsonProperty("error")
        private ErrorResponse error;
        
        /**
         * Default constructor for JSON deserialization.
         */
        public BulkEmailResult() {
        }
        
        /**
         * Creates a successful result.
         * 
         * @param result the email response
         */
        public BulkEmailResult(SendEmailResponse result) {
            this.success = true;
            this.result = result;
        }
        
        /**
         * Creates a failed result.
         * 
         * @param error the error response
         */
        public BulkEmailResult(ErrorResponse error) {
            this.success = false;
            this.error = error;
        }
        
        /**
         * Returns whether the email was sent successfully.
         * 
         * @return true if successful, false otherwise
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
         * Returns the email response for successful sends.
         * 
         * @return the email response, or null if the send failed
         */
        public SendEmailResponse getResult() {
            return result;
        }
        
        /**
         * Sets the email response.
         * 
         * @param result the email response
         */
        public void setResult(SendEmailResponse result) {
            this.result = result;
        }
        
        /**
         * Returns the error response for failed sends.
         * 
         * @return the error response, or null if the send was successful
         */
        public ErrorResponse getError() {
            return error;
        }
        
        /**
         * Sets the error response.
         * 
         * @param error the error response
         */
        public void setError(ErrorResponse error) {
            this.error = error;
        }
    }
}