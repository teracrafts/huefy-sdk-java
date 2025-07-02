package com.teracrafts.huefy.models;

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