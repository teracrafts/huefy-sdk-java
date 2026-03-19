package com.huefy.models;

public record HealthResponse(
    boolean success,
    HealthResponseData data,
    String correlationId
) {}
