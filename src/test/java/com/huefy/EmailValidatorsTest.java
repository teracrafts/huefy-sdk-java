package com.huefy;

import com.huefy.validators.EmailValidators;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link EmailValidators}.
 */
class EmailValidatorsTest {

    @Nested
    @DisplayName("validateEmail")
    class ValidateEmail {

        @Test
        @DisplayName("should accept a valid email")
        void shouldAcceptValidEmail() {
            assertNull(EmailValidators.validateEmail("user@example.com"));
        }

        @Test
        @DisplayName("should accept email with subdomain")
        void shouldAcceptSubdomainEmail() {
            assertNull(EmailValidators.validateEmail("user@mail.example.com"));
        }

        @Test
        @DisplayName("should accept email with plus addressing")
        void shouldAcceptPlusAddressing() {
            assertNull(EmailValidators.validateEmail("user+tag@example.com"));
        }

        @Test
        @DisplayName("should reject null email")
        void shouldRejectNull() {
            assertEquals("Recipient email is required", EmailValidators.validateEmail(null));
        }

        @Test
        @DisplayName("should reject empty email")
        void shouldRejectEmpty() {
            assertEquals("Recipient email is required", EmailValidators.validateEmail(""));
        }

        @Test
        @DisplayName("should reject blank email")
        void shouldRejectBlank() {
            assertEquals("Recipient email is required", EmailValidators.validateEmail("   "));
        }

        @Test
        @DisplayName("should reject email without @")
        void shouldRejectWithoutAt() {
            String result = EmailValidators.validateEmail("userexample.com");
            assertNotNull(result);
            assertTrue(result.startsWith("Invalid email address"));
        }

        @Test
        @DisplayName("should reject email without domain")
        void shouldRejectWithoutDomain() {
            String result = EmailValidators.validateEmail("user@");
            assertNotNull(result);
            assertTrue(result.startsWith("Invalid email address"));
        }

        @Test
        @DisplayName("should reject email exceeding max length")
        void shouldRejectExceedingMaxLength() {
            String longEmail = "a".repeat(250) + "@b.com";
            String result = EmailValidators.validateEmail(longEmail);
            assertNotNull(result);
            assertTrue(result.contains("maximum length"));
        }
    }

    @Nested
    @DisplayName("validateTemplateKey")
    class ValidateTemplateKey {

        @Test
        @DisplayName("should accept a valid template key")
        void shouldAcceptValid() {
            assertNull(EmailValidators.validateTemplateKey("welcome-email"));
        }

        @Test
        @DisplayName("should reject null template key")
        void shouldRejectNull() {
            assertEquals("Template key is required", EmailValidators.validateTemplateKey(null));
        }

        @Test
        @DisplayName("should reject empty template key")
        void shouldRejectEmpty() {
            assertEquals("Template key is required", EmailValidators.validateTemplateKey(""));
        }

        @Test
        @DisplayName("should reject blank template key")
        void shouldRejectBlank() {
            assertEquals("Template key is required", EmailValidators.validateTemplateKey("   "));
        }

        @Test
        @DisplayName("should reject template key exceeding max length")
        void shouldRejectExceedingMaxLength() {
            String longKey = "k".repeat(101);
            String result = EmailValidators.validateTemplateKey(longKey);
            assertNotNull(result);
            assertTrue(result.contains("maximum length"));
        }
    }

    @Nested
    @DisplayName("validateEmailData")
    class ValidateEmailData {

        @Test
        @DisplayName("should accept valid data map")
        void shouldAcceptValid() {
            assertNull(EmailValidators.validateEmailData(Map.of("name", "John")));
        }

        @Test
        @DisplayName("should accept empty data map")
        void shouldAcceptEmpty() {
            assertNull(EmailValidators.validateEmailData(Map.of()));
        }

        @Test
        @DisplayName("should reject null data")
        void shouldRejectNull() {
            assertEquals("Template data is required", EmailValidators.validateEmailData(null));
        }
    }

    @Nested
    @DisplayName("validateBulkCount")
    class ValidateBulkCount {

        @Test
        @DisplayName("should accept count of 1")
        void shouldAcceptOne() {
            assertNull(EmailValidators.validateBulkCount(1));
        }

        @Test
        @DisplayName("should accept count of 100")
        void shouldAcceptMax() {
            assertNull(EmailValidators.validateBulkCount(100));
        }

        @Test
        @DisplayName("should accept count of 50")
        void shouldAcceptMiddle() {
            assertNull(EmailValidators.validateBulkCount(50));
        }

        @Test
        @DisplayName("should reject count of 0")
        void shouldRejectZero() {
            String result = EmailValidators.validateBulkCount(0);
            assertNotNull(result);
            assertTrue(result.contains("At least one email"));
        }

        @Test
        @DisplayName("should reject negative count")
        void shouldRejectNegative() {
            String result = EmailValidators.validateBulkCount(-1);
            assertNotNull(result);
            assertTrue(result.contains("At least one email"));
        }

        @Test
        @DisplayName("should reject count exceeding 100")
        void shouldRejectExceedingMax() {
            String result = EmailValidators.validateBulkCount(101);
            assertNotNull(result);
            assertTrue(result.contains("Maximum of 100"));
        }
    }

    @Nested
    @DisplayName("validateSendEmailInput")
    class ValidateSendEmailInput {

        @Test
        @DisplayName("should return empty list for valid input")
        void shouldReturnEmptyForValid() {
            List<String> errors = EmailValidators.validateSendEmailInput(
                    "welcome", Map.of("name", "John"), "john@example.com"
            );
            assertTrue(errors.isEmpty());
        }

        @Test
        @DisplayName("should return all errors for completely invalid input")
        void shouldReturnAllErrors() {
            List<String> errors = EmailValidators.validateSendEmailInput(null, null, null);
            assertEquals(3, errors.size());
        }

        @Test
        @DisplayName("should return single error for one invalid field")
        void shouldReturnSingleError() {
            List<String> errors = EmailValidators.validateSendEmailInput(
                    "welcome", Map.of("name", "John"), "invalid-email"
            );
            assertEquals(1, errors.size());
            assertTrue(errors.get(0).startsWith("Invalid email address"));
        }

        @Test
        @DisplayName("should return two errors for two invalid fields")
        void shouldReturnTwoErrors() {
            List<String> errors = EmailValidators.validateSendEmailInput(
                    "", null, "john@example.com"
            );
            assertEquals(2, errors.size());
        }
    }
}
