package dev.huefy.sdk.models;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class EmailProviderTest {
    
    @Test
    void getValue_returnsCorrectStringValues() {
        assertThat(EmailProvider.SES.getValue()).isEqualTo("ses");
        assertThat(EmailProvider.SENDGRID.getValue()).isEqualTo("sendgrid");
        assertThat(EmailProvider.MAILGUN.getValue()).isEqualTo("mailgun");
        assertThat(EmailProvider.MAILCHIMP.getValue()).isEqualTo("mailchimp");
    }
    
    @Test
    void toString_returnsStringValue() {
        assertThat(EmailProvider.SES.toString()).isEqualTo("ses");
        assertThat(EmailProvider.SENDGRID.toString()).isEqualTo("sendgrid");
        assertThat(EmailProvider.MAILGUN.toString()).isEqualTo("mailgun");
        assertThat(EmailProvider.MAILCHIMP.toString()).isEqualTo("mailchimp");
    }
    
    @Test
    void fromValue_withValidValues_returnsCorrectProvider() {
        assertThat(EmailProvider.fromValue("ses")).isEqualTo(EmailProvider.SES);
        assertThat(EmailProvider.fromValue("sendgrid")).isEqualTo(EmailProvider.SENDGRID);
        assertThat(EmailProvider.fromValue("mailgun")).isEqualTo(EmailProvider.MAILGUN);
        assertThat(EmailProvider.fromValue("mailchimp")).isEqualTo(EmailProvider.MAILCHIMP);
    }
    
    @Test
    void fromValue_withCaseInsensitiveValues_returnsCorrectProvider() {
        assertThat(EmailProvider.fromValue("SES")).isEqualTo(EmailProvider.SES);
        assertThat(EmailProvider.fromValue("SendGrid")).isEqualTo(EmailProvider.SENDGRID);
        assertThat(EmailProvider.fromValue("MAILGUN")).isEqualTo(EmailProvider.MAILGUN);
        assertThat(EmailProvider.fromValue("MailChimp")).isEqualTo(EmailProvider.MAILCHIMP);
    }
    
    @Test
    void fromValue_withNullValue_returnsNull() {
        assertThat(EmailProvider.fromValue(null)).isNull();
    }
    
    @Test
    void fromValue_withInvalidValue_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> EmailProvider.fromValue("invalid"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid email provider: invalid");
    }
    
    @Test
    void fromValue_withEmptyValue_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> EmailProvider.fromValue(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid email provider:");
    }
}