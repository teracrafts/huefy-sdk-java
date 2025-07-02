package com.teracrafts.huefy.models;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class SendEmailRequestTest {
    
    @Test
    void builder_withValidData_createsRequest() {
        // Arrange & Act
        SendEmailRequest request = SendEmailRequest.builder()
            .templateKey("welcome-email")
            .recipient("john@example.com")
            .data(Map.of("name", "John Doe", "company", "Acme Corp"))
            .provider(EmailProvider.SENDGRID)
            .build();
        
        // Assert
        assertThat(request.getTemplateKey()).isEqualTo("welcome-email");
        assertThat(request.getRecipient()).isEqualTo("john@example.com");
        assertThat(request.getData()).containsEntry("name", "John Doe");
        assertThat(request.getData()).containsEntry("company", "Acme Corp");
        assertThat(request.getProvider()).isEqualTo(EmailProvider.SENDGRID);
    }
    
    @Test
    void builder_withoutProvider_createsRequestWithNullProvider() {
        // Arrange & Act
        SendEmailRequest request = SendEmailRequest.builder()
            .templateKey("welcome-email")
            .recipient("john@example.com")
            .data(Map.of("name", "John Doe"))
            .build();
        
        // Assert
        assertThat(request.getProvider()).isNull();
    }
    
    @Test
    void validate_withValidRequest_doesNotThrow() {
        // Arrange
        SendEmailRequest request = SendEmailRequest.builder()
            .templateKey("welcome-email")
            .recipient("john@example.com")
            .data(Map.of("name", "John Doe"))
            .build();
        
        // Act & Assert
        assertThatCode(request::validate).doesNotThrowAnyException();
    }
    
    @Test
    void validate_withNullTemplateKey_throwsIllegalArgumentException() {
        // Arrange
        SendEmailRequest request = SendEmailRequest.builder()
            .templateKey(null)
            .recipient("john@example.com")
            .data(Map.of("name", "John Doe"))
            .build();
        
        // Act & Assert
        assertThatThrownBy(request::validate)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Template key is required");
    }
    
    @Test
    void validate_withEmptyTemplateKey_throwsIllegalArgumentException() {
        // Arrange
        SendEmailRequest request = SendEmailRequest.builder()
            .templateKey("")
            .recipient("john@example.com")
            .data(Map.of("name", "John Doe"))
            .build();
        
        // Act & Assert
        assertThatThrownBy(request::validate)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Template key is required");
    }
    
    @Test
    void validate_withWhitespaceTemplateKey_throwsIllegalArgumentException() {
        // Arrange
        SendEmailRequest request = SendEmailRequest.builder()
            .templateKey("   ")
            .recipient("john@example.com")
            .data(Map.of("name", "John Doe"))
            .build();
        
        // Act & Assert
        assertThatThrownBy(request::validate)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Template key is required");
    }
    
    @Test
    void validate_withNullRecipient_throwsIllegalArgumentException() {
        // Arrange
        SendEmailRequest request = SendEmailRequest.builder()
            .templateKey("welcome-email")
            .recipient(null)
            .data(Map.of("name", "John Doe"))
            .build();
        
        // Act & Assert
        assertThatThrownBy(request::validate)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Recipient is required");
    }
    
    @Test
    void validate_withEmptyRecipient_throwsIllegalArgumentException() {
        // Arrange
        SendEmailRequest request = SendEmailRequest.builder()
            .templateKey("welcome-email")
            .recipient("")
            .data(Map.of("name", "John Doe"))
            .build();
        
        // Act & Assert
        assertThatThrownBy(request::validate)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Recipient is required");
    }
    
    @Test
    void validate_withInvalidEmailFormat_throwsIllegalArgumentException() {
        // Arrange
        SendEmailRequest request = SendEmailRequest.builder()
            .templateKey("welcome-email")
            .recipient("invalid-email")
            .data(Map.of("name", "John Doe"))
            .build();
        
        // Act & Assert
        assertThatThrownBy(request::validate)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid recipient email address");
    }
    
    @Test
    void validate_withValidEmailFormats_doesNotThrow() {
        String[] validEmails = {
            "john@example.com",
            "john.doe@example.com",
            "john+test@example.co.uk",
            "user123@test-domain.org",
            "test_email@domain.info"
        };
        
        for (String email : validEmails) {
            SendEmailRequest request = SendEmailRequest.builder()
                .templateKey("welcome-email")
                .recipient(email)
                .data(Map.of("name", "John Doe"))
                .build();
            
            assertThatCode(request::validate)
                .as("Email %s should be valid", email)
                .doesNotThrowAnyException();
        }
    }
    
    @Test
    void validate_withInvalidEmailFormats_throwsIllegalArgumentException() {
        String[] invalidEmails = {
            "invalid-email",
            "@example.com",
            "john@",
            "john..doe@example.com",
            "john@example",
            "",
            "   "
        };
        
        for (String email : invalidEmails) {
            SendEmailRequest request = SendEmailRequest.builder()
                .templateKey("welcome-email")
                .recipient(email)
                .data(Map.of("name", "John Doe"))
                .build();
            
            assertThatThrownBy(request::validate)
                .as("Email %s should be invalid", email)
                .isInstanceOf(IllegalArgumentException.class);
        }
    }
    
    @Test
    void validate_withNullData_throwsIllegalArgumentException() {
        // Arrange
        SendEmailRequest request = SendEmailRequest.builder()
            .templateKey("welcome-email")
            .recipient("john@example.com")
            .data(null)
            .build();
        
        // Act & Assert
        assertThatThrownBy(request::validate)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Template data is required");
    }
    
    @Test
    void validate_withEmptyData_throwsIllegalArgumentException() {
        // Arrange
        SendEmailRequest request = SendEmailRequest.builder()
            .templateKey("welcome-email")
            .recipient("john@example.com")
            .data(Map.of())
            .build();
        
        // Act & Assert
        assertThatThrownBy(request::validate)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Template data is required");
    }
}