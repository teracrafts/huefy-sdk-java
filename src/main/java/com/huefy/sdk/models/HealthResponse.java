package dev.huefy.sdk.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Response object for API health check operations.
 * 
 * <p>This class represents the response from the Huefy API health check endpoint.
 * It provides information about the current status and health of the API service.</p>
 * 
 * @author Huefy Team
 * @since 1.0.0
 */
public class HealthResponse {
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    @JsonProperty("version")
    private String version;
    
    /**
     * Default constructor for JSON deserialization.
     */
    public HealthResponse() {
    }
    
    /**
     * Creates a new HealthResponse.
     * 
     * @param status the health status
     * @param timestamp the timestamp of the health check
     * @param version the API version
     */
    public HealthResponse(String status, Instant timestamp, String version) {
        this.status = status;
        this.timestamp = timestamp;
        this.version = version;
    }
    
    /**
     * Returns the health status of the API.
     * 
     * @return the health status (e.g., "healthy", "degraded", "unhealthy")
     */
    public String getStatus() {
        return status;
    }
    
    /**
     * Sets the health status.
     * 
     * @param status the health status
     */
    public void setStatus(String status) {
        this.status = status;
    }
    
    /**
     * Returns the timestamp when the health check was performed.
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
    
    /**
     * Returns the API version.
     * 
     * @return the API version, or null if not provided
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * Sets the API version.
     * 
     * @param version the API version
     */
    public void setVersion(String version) {
        this.version = version;
    }
    
    @Override
    public String toString() {
        return "HealthResponse{" +
                "status='" + status + '\'' +
                ", timestamp=" + timestamp +
                ", version='" + version + '\'' +
                '}';
    }
}