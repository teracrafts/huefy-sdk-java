package com.teracrafts.huefy.models;

import java.util.List;

public record SendBulkEmailsRequest(
    String templateKey,
    List<BulkRecipient> recipients,
    EmailProvider provider
) {
    public SendBulkEmailsRequest(String templateKey, List<BulkRecipient> recipients) {
        this(templateKey, recipients, null);
    }
}
