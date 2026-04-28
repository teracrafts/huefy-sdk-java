package com.teracrafts.huefy.models;

public record SendEmailResponse(
    boolean success,
    SendEmailResponseData data,
    String correlationId
) {}
