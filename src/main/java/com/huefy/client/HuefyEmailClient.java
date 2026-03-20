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
 * // Bulk emails
 * var recipients = List.of(new BulkRecipient("alice@example.com", "to", Map.of("name", "Alice")));
 * var result = client.sendBulkEmails("welcome", recipients);
 * }</pre>
 */
public class HuefyEmailClient extends HuefyClient {

    private static final Logger logger = LoggerFactory.getLogger(HuefyEmailClient.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String EMAILS_SEND_PATH = "/emails/send";
    private static final String EMAILS_SEND_BULK_PATH = "/emails/send-bulk";

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

            JsonNode dataNode2 = responseNode.path("data");
            List<RecipientStatus> recipients = new ArrayList<>();
            if (dataNode2.has("recipients") && dataNode2.get("recipients").isArray()) {
                for (JsonNode r : dataNode2.get("recipients")) {
                    recipients.add(new RecipientStatus(
                            r.has("email") ? r.get("email").asText() : null,
                            r.has("status") ? r.get("status").asText() : null,
                            r.has("messageId") ? r.get("messageId").asText() : null,
                            r.has("error") ? r.get("error").asText() : null,
                            r.has("sentAt") ? r.get("sentAt").asText() : null
                    ));
                }
            }

            SendEmailResponseData emailData = new SendEmailResponseData(
                    dataNode2.has("emailId") ? dataNode2.get("emailId").asText() : null,
                    dataNode2.has("status") ? dataNode2.get("status").asText() : null,
                    recipients,
                    dataNode2.has("scheduledAt") ? dataNode2.get("scheduledAt").asText() : null,
                    dataNode2.has("sentAt") ? dataNode2.get("sentAt").asText() : null
            );

            return new SendEmailResponse(
                    responseNode.has("success") && responseNode.get("success").asBoolean(),
                    emailData,
                    responseNode.has("correlationId") ? responseNode.get("correlationId").asText() : null
            );

        } catch (HuefyException e) {
            throw e;
        } catch (Exception e) {
            throw HuefyException.networkError("Failed to send email: " + e.getMessage(), e);
        }
    }

    /**
     * Sends multiple emails in bulk using a shared template.
     *
     * @param templateKey the template key to use for all recipients
     * @param recipients  the list of bulk recipients
     * @return the bulk send response
     * @throws HuefyException if validation fails or the request fails
     */
    public SendBulkEmailsResponse sendBulkEmails(String templateKey, List<BulkRecipient> recipients) {
        return sendBulkEmails(templateKey, recipients, null, null, null, null, null);
    }

    /**
     * Sends multiple emails in bulk using a shared template with optional settings.
     *
     * @param templateKey  the template key to use for all recipients
     * @param recipients   the list of bulk recipients
     * @param fromEmail    optional sender email address
     * @param fromName     optional sender name
     * @param providerType optional email provider type
     * @param batchSize    optional batch size
     * @param correlationId optional correlation ID
     * @return the bulk send response
     * @throws HuefyException if the request fails
     */
    public SendBulkEmailsResponse sendBulkEmails(String templateKey, List<BulkRecipient> recipients,
                                                   String fromEmail, String fromName,
                                                   String providerType, Integer batchSize,
                                                   String correlationId) {
        Objects.requireNonNull(templateKey, "templateKey must not be null");
        Objects.requireNonNull(recipients, "recipients must not be null");

        for (int i = 0; i < recipients.size(); i++) {
            String emailErr = EmailValidators.validateEmail(recipients.get(i).email());
            if (emailErr != null) {
                throw new HuefyException(
                        "recipients[" + i + "]: " + emailErr,
                        ErrorCode.VALIDATION_ERROR,
                        null,
                        false
                );
            }
        }

        try {
            SendBulkEmailsRequest request = new SendBulkEmailsRequest(
                    templateKey, recipients, fromEmail, fromName, providerType, batchSize, correlationId
            );

            ObjectNode body = objectMapper.createObjectNode();
            body.put("templateKey", request.templateKey());

            ArrayNode recipientsNode = objectMapper.createArrayNode();
            for (BulkRecipient r : request.recipients()) {
                ObjectNode rNode = objectMapper.createObjectNode();
                rNode.put("email", r.email());
                if (r.type() != null) rNode.put("type", r.type());
                if (r.data() != null) rNode.set("data", objectMapper.valueToTree(r.data()));
                recipientsNode.add(rNode);
            }
            body.set("recipients", recipientsNode);

            if (request.fromEmail() != null) body.put("fromEmail", request.fromEmail());
            if (request.fromName() != null) body.put("fromName", request.fromName());
            if (request.providerType() != null) body.put("providerType", request.providerType());
            if (request.batchSize() != null) body.put("batchSize", request.batchSize());
            if (request.correlationId() != null) body.put("correlationId", request.correlationId());

            logger.debug("Sending bulk emails using template '{}'", templateKey);
            String responseBody = httpClient.request("POST", EMAILS_SEND_BULK_PATH, body.toString());
            JsonNode responseNode = objectMapper.readTree(responseBody);

            JsonNode dataNode = responseNode.path("data");
            List<RecipientStatus> recipientStatuses = new ArrayList<>();
            if (dataNode.has("recipients") && dataNode.get("recipients").isArray()) {
                for (JsonNode r : dataNode.get("recipients")) {
                    recipientStatuses.add(new RecipientStatus(
                            r.has("email") ? r.get("email").asText() : null,
                            r.has("status") ? r.get("status").asText() : null,
                            r.has("messageId") ? r.get("messageId").asText() : null,
                            r.has("error") ? r.get("error").asText() : null,
                            r.has("sentAt") ? r.get("sentAt").asText() : null
                    ));
                }
            }

            SendBulkEmailsResponseData bulkData = new SendBulkEmailsResponseData(
                    dataNode.has("batchId") ? dataNode.get("batchId").asText() : null,
                    dataNode.has("status") ? dataNode.get("status").asText() : null,
                    dataNode.has("templateKey") ? dataNode.get("templateKey").asText() : null,
                    dataNode.has("totalRecipients") ? dataNode.get("totalRecipients").asInt() : 0,
                    dataNode.has("successCount") ? dataNode.get("successCount").asInt() : 0,
                    dataNode.has("failureCount") ? dataNode.get("failureCount").asInt() : 0,
                    dataNode.has("suppressedCount") ? dataNode.get("suppressedCount").asInt() : 0,
                    dataNode.has("startedAt") ? dataNode.get("startedAt").asText() : null,
                    dataNode.has("completedAt") ? dataNode.get("completedAt").asText() : null,
                    recipientStatuses
            );

            return new SendBulkEmailsResponse(
                    responseNode.has("success") && responseNode.get("success").asBoolean(),
                    bulkData,
                    responseNode.has("correlationId") ? responseNode.get("correlationId").asText() : null
            );

        } catch (HuefyException e) {
            throw e;
        } catch (Exception e) {
            throw HuefyException.networkError("Failed to send bulk emails: " + e.getMessage(), e);
        }
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
