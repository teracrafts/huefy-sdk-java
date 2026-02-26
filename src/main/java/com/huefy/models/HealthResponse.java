package com.huefy.models;

public record HealthResponse(String status, String timestamp, String version) {
    public boolean isHealthy() { return "ok".equalsIgnoreCase(status); }
}
