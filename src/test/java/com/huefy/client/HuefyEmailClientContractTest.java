package com.huefy.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.huefy.models.EmailProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HuefyEmailClientContractTest {

    @Test
    @DisplayName("single-send body uses camelCase transport keys and preserves JSON values")
    void singleSendBodyUsesCamelCaseKeys() {
        JsonNode body = HuefyEmailClient.buildSendEmailBody(
                "welcome",
                Map.of(
                        "name", "John",
                        "count", 2,
                        "beta", true,
                        "roles", List.of("admin", "editor")
                ),
                "john@example.com",
                EmailProvider.SENDGRID
        );

        assertEquals("welcome", body.get("templateKey").asText());
        assertEquals("john@example.com", body.get("recipient").asText());
        assertEquals("sendgrid", body.get("providerType").asText());
        assertNull(body.get("template_key"));
        assertNull(body.get("provider"));

        JsonNode data = body.get("data");
        assertEquals("John", data.get("name").asText());
        assertEquals(2, data.get("count").asInt());
        assertTrue(data.get("beta").asBoolean());
        assertEquals("admin", data.get("roles").get(0).asText());
    }
}
