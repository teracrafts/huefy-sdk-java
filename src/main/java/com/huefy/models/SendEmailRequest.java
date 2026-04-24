package com.huefy.models;

import java.util.Map;

public record SendEmailRequest(
    String templateKey,
    Map<String, String> data,
    String recipient,
    EmailProvider provider
) {
    public SendEmailRequest(String templateKey, Map<String, String> data, String recipient) {
        this(templateKey, data, recipient, null);
    }
}
