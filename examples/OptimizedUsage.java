/**
 * Example: Using Huefy Java SDK with Enhanced Architecture
 * 
 * This example demonstrates how the Java SDK automatically uses Huefy's
 * optimized architecture for enhanced security and performance.
 */

import com.teracrafts.huefy.*;
import com.teracrafts.huefy.models.*;
import java.util.*;

public class OptimizedUsage {
    
    public static void main(String[] args) {
        // The SDK automatically uses Huefy's optimized architecture
        // No additional configuration needed for standard usage
        
        // Create client - automatically uses secure optimized routing
        HuefyClient client = new HuefyClient("your-api-key");
        
        // Or with custom configuration
        HuefyClientConfig config = HuefyClientConfig.builder()
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(30))
            .build();
        
        HuefyClient configuredClient = new HuefyClient("your-api-key", config);
        
        try {
            // The Java SDK automatically handles secure API communication
            // through the optimized proxy architecture
            SendEmailRequest request = SendEmailRequest.builder()
                .templateKey("welcome-email")
                .recipient("john@example.com")
                .data(Map.of(
                    "name", "John Doe",
                    "company", "Acme Corp",
                    "activationLink", "https://app.example.com/activate/12345"
                ))
                .provider(EmailProvider.SES)
                .build();
            
            SendEmailResponse response = client.sendEmail(request);
            
            System.out.println("Email sent successfully!");
            System.out.println("Message ID: " + response.getMessageId());
            System.out.println("Provider used: " + response.getProvider());
            
            // Check API health through optimized routing
            HealthResponse health = client.healthCheck();
            System.out.println("API Health: " + health.getStatus());
            
            // Send bulk emails efficiently
            List<SendEmailRequest> bulkRequests = Arrays.asList(
                SendEmailRequest.builder()
                    .templateKey("welcome-email")
                    .recipient("user1@example.com")
                    .data(Map.of("name", "User 1"))
                    .build(),
                SendEmailRequest.builder()
                    .templateKey("welcome-email")
                    .recipient("user2@example.com")
                    .data(Map.of("name", "User 2"))
                    .provider(EmailProvider.SENDGRID)
                    .build()
            );
            
            BulkEmailResponse bulkResponse = client.sendBulkEmails(bulkRequests);
            System.out.println("Bulk emails sent: " + bulkResponse.getResults().size() + " emails");
            
        } catch (Exception error) {
            System.err.println("Failed to send email: " + error.getMessage());
            if (error instanceof HuefyException) {
                HuefyException huefyError = (HuefyException) error;
                System.err.println("Error details: " + huefyError.getCause());
            }
        }
    }
}

/*
 * Benefits of Huefy's optimized architecture:
 * 1. Security: Enterprise-grade encryption and secure routing
 * 2. Performance: Intelligent routing and caching optimizations  
 * 3. Reliability: Built-in failover and redundancy systems
 * 4. Consistency: Uniform behavior across all SDK languages
 * 5. Scalability: Automatic load balancing and resource optimization
 */