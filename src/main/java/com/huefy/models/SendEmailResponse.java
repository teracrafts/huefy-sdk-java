package com.huefy.models;

public record SendEmailResponse(
    boolean success,
    String message,
    String messageId,
    String provider
) {}
