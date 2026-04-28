package com.teracrafts.huefy.models;

public record SendBulkEmailsResponse(
    boolean success,
    SendBulkEmailsResponseData data,
    String correlationId
) {}
