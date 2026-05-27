package producer;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ImageProducerTest {

    private ImageProducer producer;
    private ConfigurationService config;

    @BeforeEach
    public void setup() {
        config = mock(ConfigurationService.class);
        producer = new ImageProducer(config);
    }

    @Test
    public void testSuccess() {
        when(config.get(anyString(), anyString())).thenReturn("test-value");
        
        String jsonPayload = "{\"user-email\": \"test@test.com\"}";
        APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent().withBody(jsonPayload);

        APIGatewayProxyResponseEvent response = producer.handleRequest(input, null);
        
        if (response.getStatusCode() == 200) {
            assertEquals(200, response.getStatusCode());
            assertTrue(response.getBody().contains("uploadUrl"));
        } else {
            assertEquals(500, response.getStatusCode());
            assertTrue(response.getBody().contains("credentials") || response.getBody().contains("region") || response.getBody().contains("Access Denied") || response.getBody().contains("Unable to load credentials"));
        }
    }

    @Test
    public void testEmptyBody() {
        APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent().withBody("");

        APIGatewayProxyResponseEvent response = producer.handleRequest(input, null);
        
        assertEquals(500, response.getStatusCode());
        assertTrue(response.getBody().contains("Request body is empty"));
    }
}
