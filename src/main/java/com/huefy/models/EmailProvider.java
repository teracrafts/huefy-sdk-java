package com.huefy.models;

public enum EmailProvider {
    SES("ses"),
    SENDGRID("sendgrid"),
    MAILGUN("mailgun"),
    MAILCHIMP("mailchimp");

    private final String value;

    EmailProvider(String value) { this.value = value; }
    public String getValue() { return value; }

    public static EmailProvider fromValue(String value) {
        for (EmailProvider p : values()) {
            if (p.value.equalsIgnoreCase(value)) return p;
        }
        throw new IllegalArgumentException("Unknown provider: " + value);
    }
}
