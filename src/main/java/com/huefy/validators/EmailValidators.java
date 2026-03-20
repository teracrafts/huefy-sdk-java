package com.huefy.validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class EmailValidators {
    private static final Pattern EMAIL_REGEX = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int MAX_EMAIL_LENGTH = 254;
    private static final int MAX_TEMPLATE_KEY_LENGTH = 100;
    private static final int MAX_BULK_EMAILS = 1000;

    private EmailValidators() {}

    public static String validateEmail(String email) {
        if (email == null || email.isBlank()) return "Recipient email is required";
        String trimmed = email.trim();
        if (trimmed.length() > MAX_EMAIL_LENGTH) return "Email exceeds maximum length of " + MAX_EMAIL_LENGTH + " characters";
        if (!EMAIL_REGEX.matcher(trimmed).matches()) return "Invalid email address: " + trimmed;
        return null;
    }

    public static String validateTemplateKey(String key) {
        if (key == null || key.isBlank()) return "Template key is required";
        if (key.trim().length() > MAX_TEMPLATE_KEY_LENGTH) return "Template key exceeds maximum length of " + MAX_TEMPLATE_KEY_LENGTH + " characters";
        return null;
    }

    public static String validateEmailData(Map<String, String> data) {
        if (data == null) return "Template data is required";
        return null;
    }

    public static String validateBulkCount(int count) {
        if (count <= 0) return "At least one email is required";
        if (count > MAX_BULK_EMAILS) return "Maximum of " + MAX_BULK_EMAILS + " emails per bulk request";
        return null;
    }

    public static List<String> validateSendEmailInput(String templateKey, Map<String, String> data, String recipient) {
        List<String> errors = new ArrayList<>();
        String keyErr = validateTemplateKey(templateKey);
        if (keyErr != null) errors.add(keyErr);
        String dataErr = validateEmailData(data);
        if (dataErr != null) errors.add(dataErr);
        String emailErr = validateEmail(recipient);
        if (emailErr != null) errors.add(emailErr);
        return errors;
    }
}
