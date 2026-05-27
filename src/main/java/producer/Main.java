package producer;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public class Main {
    public static void main(String[] args) {
        System.out.println("Simulating Lambda execution...");

        ImageProducer producer = new ImageProducer();

        String jsonPayload = "{\"user-email\": \"dev@test.com\"}";
        
        APIGatewayProxyRequestEvent mockRequest = new APIGatewayProxyRequestEvent()
                .withBody(jsonPayload);

        APIGatewayProxyResponseEvent response = producer.handleRequest(mockRequest, null);
        
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Body: " + response.getBody());
    }
}
