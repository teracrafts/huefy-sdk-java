package com.teracrafts.huefy.models;

import java.util.Map;

public final class SendEmailRequest {
    private final String templateKey;
    private final Map<String, ?> data;
    private final String recipient;
    private final SendEmailRecipient recipientObject;
    private final EmailProvider provider;

    public SendEmailRequest(String templateKey, Map<String, ?> data, String recipient) {
        this(templateKey, data, recipient, null, null);
    }

    public SendEmailRequest(String templateKey, Map<String, ?> data, String recipient, EmailProvider provider) {
        this(templateKey, data, recipient, null, provider);
    }

    public SendEmailRequest(String templateKey, Map<String, ?> data, SendEmailRecipient recipient) {
        this(templateKey, data, null, recipient, null);
    }

    public SendEmailRequest(String templateKey, Map<String, ?> data, SendEmailRecipient recipient, EmailProvider provider) {
        this(templateKey, data, null, recipient, provider);
    }

    private SendEmailRequest(
            String templateKey,
            Map<String, ?> data,
            String recipient,
            SendEmailRecipient recipientObject,
            EmailProvider provider
    ) {
        this.templateKey = templateKey;
        this.data = data;
        this.recipient = recipient;
        this.recipientObject = recipientObject;
        this.provider = provider;
    }

    public String templateKey() {
        return templateKey;
    }

    public Map<String, ?> data() {
        return data;
    }

    public String recipient() {
        return recipient;
    }

    public SendEmailRecipient recipientObject() {
        return recipientObject;
    }

    public EmailProvider provider() {
        return provider;
    }
}
