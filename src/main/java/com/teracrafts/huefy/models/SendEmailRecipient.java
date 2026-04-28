package com.teracrafts.huefy.models;

import java.util.Map;

public record SendEmailRecipient(
    String email,
    String type,
    Map<String, ?> data
) {
    public SendEmailRecipient(String email) {
        this(email, null, null);
    }

    public SendEmailRecipient(String email, String type) {
        this(email, type, null);
    }
}
