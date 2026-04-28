package com.teracrafts.huefy.models;

import java.util.List;
import java.util.Map;

public record SendBulkEmailsResponseData(
    String batchId,
    String status,
    String templateKey,
    int templateVersion,
    String senderUsed,
    boolean senderVerified,
    int totalRecipients,
    int processedCount,
    int successCount,
    int failureCount,
    int suppressedCount,
    String startedAt,
    String completedAt,
    List<RecipientStatus> recipients,
    List<Map<String, Object>> errors,
    Map<String, Object> metadata
) {}
