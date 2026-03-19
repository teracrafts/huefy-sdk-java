package com.huefy.models;

import java.util.List;

public record SendBulkEmailsResponseData(
    String batchId,
    String status,
    String templateKey,
    int totalRecipients,
    int successCount,
    int failureCount,
    int suppressedCount,
    String startedAt,
    String completedAt,
    List<RecipientStatus> recipients
) {}
