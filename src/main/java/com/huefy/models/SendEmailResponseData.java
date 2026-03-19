package com.huefy.models;

import java.util.List;

public record SendEmailResponseData(
    String emailId,
    String status,
    List<RecipientStatus> recipients,
    String scheduledAt,
    String sentAt
) {}
