package com.huefy.models;

import java.util.List;

public record SendBulkEmailsRequest(
    String templateKey,
    List<BulkRecipient> recipients,
    String fromEmail,
    String fromName,
    String providerType,
    Integer batchSize,
    String correlationId
) {}
