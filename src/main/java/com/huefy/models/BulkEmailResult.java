package com.huefy.models;

public record BulkEmailResult(
    String email,
    boolean success,
    SendEmailResponse result,
    BulkEmailError error
) {
    public record BulkEmailError(String message, String code) {}
}
