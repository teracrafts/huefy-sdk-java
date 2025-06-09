package com.huefy.sdk.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Response object for API error responses.
 * 
 * <p>This class represents an error response from the Huefy API.
 * It contains detailed information about what went wrong, including
 * error codes, messages, and additional details.</p>
 * 
 * @author Huefy Team
 * @since 1.0.0
 */
public class ErrorResponse {
    @JsonProperty("error")
    private ErrorDetail error;
    
    /**
     * Default constructor for JSON deserialization.
     */
    public ErrorResponse() {
    }
    
    /**
     * Creates a new ErrorResponse with the specified error detail.
     * 
     * @param error the error detail
     */
    public ErrorResponse(ErrorDetail error) {
        this.error = error;
    }
    
    /**
     * Returns the error detail.
     * 
     * @return the error detail
     */
    public ErrorDetail getError() {
        return error;
    }
    
    /**
     * Sets the error detail.
     * 
     * @param error the error detail
     */
    public void setError(ErrorDetail error) {
        this.error = error;
    }
}

/**
 * Detailed information about an API error.
 * 
 * <p>This class contains the specific error code, human-readable message,
 * and any additional details that may help in troubleshooting the error.</p>
 */
class ErrorDetail {
    @JsonProperty("code")
    private String code;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("details")
    private Map<String, Object> details;
    
    /**
     * Default constructor for JSON deserialization.
     */
    public ErrorDetail() {
    }
    
    /**
     * Creates a new ErrorDetail.
     * 
     * @param code the error code
     * @param message the error message
     * @param details additional error details
     */
    public ErrorDetail(String code, String message, Map<String, Object> details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }
    
    /**
     * Returns the error code.
     * 
     * @return the error code
     */
    public String getCode() {
        return code;
    }
    
    /**
     * Sets the error code.
     * 
     * @param code the error code
     */
    public void setCode(String code) {
        this.code = code;
    }
    
    /**
     * Returns the error message.
     * 
     * @return the error message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Sets the error message.
     * 
     * @param message the error message
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    /**
     * Returns additional error details.
     * 
     * @return the error details, or null if no details are available
     */
    public Map<String, Object> getDetails() {
        return details;
    }
    
    /**
     * Sets additional error details.
     * 
     * @param details the error details
     */
    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
}