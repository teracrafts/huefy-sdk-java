package com.huefy.models;

public record SendBulkEmailsResponse(
    boolean success,
    SendBulkEmailsResponseData data,
    String correlationId
) {}
