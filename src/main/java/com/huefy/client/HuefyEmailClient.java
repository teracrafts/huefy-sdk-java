package com.huefy.client;

import com.huefy.config.HuefyConfig;
import com.huefy.errors.ErrorCode;
import com.huefy.errors.HuefyException;
import com.huefy.models.*;
import com.huefy.security.Security;
import com.huefy.validators.EmailValidators;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Email-focused client for the Huefy SDK.
 *
 * <p>Extends {@link HuefyClient} with email-specific operations including
 * single and bulk email sending with input validation.</p>
 *
 * <pre>{@code
 * // Simple usage
 * var client = new HuefyEmailClient("your-api-key");
 * var response = client.sendEmail("welcome", Map.of("name", "John"), "john@example.com");
 *
 * // With provider
 * var response = client.sendEmail("welcome", Map.of("name", "John"), "john@example.com", EmailProvider.SENDGRID);
 *
 * // Bulk emails
 * var requests = List.of(
 *     new SendEmailRequest("welcome", "alice@example.com", Map.of("name", "Alice")),
 *     new SendEmailRequest("welcome", "bob@example.com", Map.of("name", "Bob"))
 * );
 * var results = client.sendBulkEmails(requests);
 * }</pre>
 */
public class HuefyEmailClient extends HuefyClient {

    private static final Logger logger = LoggerFactory.getLogger(HuefyEmailClient.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String EMAILS_SEND_PATH = "/emails/send";

    /**
     * Creates a new email client with the given API key and default configuration.
     *
     * @param apiKey the API key for authentication
     * @throws IllegalArgumentException if apiKey is null or blank
     */
    public HuefyEmailClient(String apiKey) {
        this(HuefyConfig.builder().apiKey(apiKey).build());
    }

    /**
     * Creates a new email client with the given configuration.
     *
     * @param config the SDK configuration
     * @throws IllegalArgumentException if config is null
     */
    public HuefyEmailClient(HuefyConfig config) {
        super(config);
    }

    /**
     * Creates a new {@link Builder} for advanced email client configuration.
     *
     * @return a new builder instance
     */
    public static Builder emailBuilder() {
        return new Builder();
    }

    /**
     * Sends an email using the default provider (SES).
     *
     * @param templateKey the template key to use
     * @param data        the template data variables
     * @param recipient   the recipient email address
     * @return the send email response
     * @throws HuefyException if validation fails or the request fails
     */
    public SendEmailResponse sendEmail(String templateKey, Map<String, String> data, String recipient) {
        return sendEmail(templateKey, data, recipient, null);
    }

    /**
     * Sends an email using the specified provider.
     *
     * @param templateKey the template key to use
     * @param data        the template data variables
     * @param recipient   the recipient email address
     * @param provider    the email provider (null for default SES)
     * @return the send email response
     * @throws HuefyException if validation fails or the request fails
     */
    public SendEmailResponse sendEmail(String templateKey, Map<String, String> data,
                                        String recipient, EmailProvider provider) {
        // Validate input
        List<String> errors = EmailValidators.validateSendEmailInput(templateKey, data, recipient);
        if (!errors.isEmpty()) {
            throw new HuefyException(
                    "Validation failed: " + String.join("; ", errors),
                    ErrorCode.VALIDATION_ERROR,
                    null,
                    false
            );
        }

        // Check template data for PII and warn (matching Go SDK behavior)
        for (Map.Entry<String, String> entry : data.entrySet()) {
            List<String> piiTypes = Security.detectPii(entry.getValue());
            if (!piiTypes.isEmpty()) {
                logger.warn("Potential PII detected in template data field '{}': {}. " +
                        "Consider removing or encrypting these fields.", entry.getKey(), piiTypes);
            }
        }

        try {
            // Build request body
            ObjectNode body = objectMapper.createObjectNode();
            body.put("template_key", templateKey.trim());
            body.put("recipient", recipient.trim());

            ObjectNode dataNode = objectMapper.createObjectNode();
            data.forEach(dataNode::put);
            body.set("data", dataNode);

            if (provider != null) {
                body.put("provider", provider.getValue());
            }

            logger.debug("Sending email to {} using template '{}'", recipient, templateKey);
            String responseBody = httpClient.request("POST", EMAILS_SEND_PATH, body.toString());
            JsonNode responseNode = objectMapper.readTree(responseBody);

            return new SendEmailResponse(
                    responseNode.has("success") && responseNode.get("success").asBoolean(),
                    responseNode.has("message") ? responseNode.get("message").asText() : null,
                    responseNode.has("message_id") ? responseNode.get("message_id").asText() : null,
                    responseNode.has("provider") ? responseNode.get("provider").asText() : null
            );

        } catch (HuefyException e) {
            throw e;
        } catch (Exception e) {
            throw HuefyException.networkError("Failed to send email: " + e.getMessage(), e);
        }
    }

    /**
     * Sends multiple emails in bulk.
     *
     * <p>Each request is sent independently. Failures for individual emails
     * do not prevent remaining emails from being sent.</p>
     *
     * @param requests the list of email requests to send
     * @return a list of results for each email
     * @throws HuefyException if the bulk count validation fails
     */
    public List<BulkEmailResult> sendBulkEmails(List<SendEmailRequest> requests) {
        Objects.requireNonNull(requests, "Requests list must not be null");

        String countError = EmailValidators.validateBulkCount(requests.size());
        if (countError != null) {
            throw new HuefyException(
                    countError,
                    ErrorCode.VALIDATION_ERROR,
                    null,
                    false
            );
        }

        List<BulkEmailResult> results = new ArrayList<>();

        for (SendEmailRequest request : requests) {
            try {
                SendEmailResponse response = sendEmail(
                        request.templateKey(),
                        request.data(),
                        request.recipient(),
                        request.provider()
                );
                results.add(new BulkEmailResult(request.recipient(), true, response, null));
            } catch (HuefyException e) {
                logger.warn("Bulk email failed for {}: {}", request.recipient(), e.getMessage());
                results.add(new BulkEmailResult(
                        request.recipient(),
                        false,
                        null,
                        new BulkEmailResult.BulkEmailError(e.getMessage(), e.getCode().name())
                ));
            }
        }

        logger.info("Bulk email complete: {}/{} succeeded",
                results.stream().filter(BulkEmailResult::success).count(),
                results.size());

        return results;
    }

    /**
     * Builder for creating {@link HuefyEmailClient} instances with advanced configuration.
     */
    public static final class Builder {

        private final HuefyConfig.Builder configBuilder = HuefyConfig.builder();

        private Builder() {}

        public Builder apiKey(String apiKey) {
            configBuilder.apiKey(apiKey);
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            configBuilder.baseUrl(baseUrl);
            return this;
        }

        public Builder timeout(long timeout) {
            configBuilder.timeout(timeout);
            return this;
        }

        public Builder secondaryApiKey(String secondaryApiKey) {
            configBuilder.secondaryApiKey(secondaryApiKey);
            return this;
        }

        public Builder enableRequestSigning(boolean enable) {
            configBuilder.enableRequestSigning(enable);
            return this;
        }

        public Builder enableErrorSanitization(boolean enable) {
            configBuilder.enableErrorSanitization(enable);
            return this;
        }

        public Builder retryConfig(HuefyConfig.RetryConfig retryConfig) {
            configBuilder.retryConfig(retryConfig);
            return this;
        }

        public Builder circuitBreakerConfig(HuefyConfig.CircuitBreakerConfig circuitBreakerConfig) {
            configBuilder.circuitBreakerConfig(circuitBreakerConfig);
            return this;
        }

        /**
         * Builds the email client.
         *
         * @return a new {@link HuefyEmailClient} instance
         */
        public HuefyEmailClient build() {
            return new HuefyEmailClient(configBuilder.build());
        }
    }
}
