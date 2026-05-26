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
    public void testMissingFileName() {
        String jsonPayload = "{\"user-email\": \"test@test.com\"}";
        APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent().withBody(jsonPayload);

        APIGatewayProxyResponseEvent response = producer.handleRequest(input, null);
        
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().contains("file-name is required"));
    }

    @Test
    public void testEmptyBody() {
        APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent().withBody("");

        APIGatewayProxyResponseEvent response = producer.handleRequest(input, null);
        
        assertEquals(500, response.getStatusCode());
        assertTrue(response.getBody().contains("Request body is empty"));
    }
}
