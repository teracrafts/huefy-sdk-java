package com.huefy.models;

import java.util.Map;

public record SendEmailRequest(
    String templateKey,
    String recipient,
    Map<String, String> data,
    EmailProvider provider
) {
    public SendEmailRequest(String templateKey, String recipient, Map<String, String> data) {
        this(templateKey, recipient, data, null);
    }
}
