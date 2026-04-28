package com.teracrafts.huefy.models;

public record RecipientStatus(
    String email,
    String status,
    String messageId,
    String error,
    String sentAt
) {}
