package producer;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

public class ImageProducer implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ConfigurationService config;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ImageProducer() {
        this.config = new ConfigurationService();
    }

    public ImageProducer(ConfigurationService config) {
        this.config = config;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        try {
            Map<String, String> data = parseBody(input);
            String fileName = data.get("file-name");
            String userEmail = data.get("user-email");

            if (fileName == null || fileName.isEmpty()) {
                return buildResponse(400, "{\"error\": \"file-name is required\"}");
            }

            String bucketName = config.get("S3_BUCKET_NAME", "S3_BUCKET_NAME");
            String regionName = config.get("APP_REGION", "AWS_REGION");
            Region region = Region.of(regionName != null ? regionName : "us-east-1");

            S3Presigner presigner = S3Presigner.builder()
                    .region(region)
                    .build();

            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .metadata(Map.of("user-email", userEmail != null ? userEmail : "unknown"))
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .putObjectRequest(objectRequest)
                    .build();

            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
            String generatedUrl = presignedRequest.url().toString();

            presigner.close();

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withHeaders(Collections.singletonMap("Content-Type", "application/json"))
                    .withBody("{\"uploadUrl\": \"" + generatedUrl + "\"}");

        } catch (Exception e) {
            return buildResponse(500, "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private Map<String, String> parseBody(APIGatewayProxyRequestEvent input) throws Exception {
        if (input == null || input.getBody() == null || input.getBody().isEmpty()) {
            throw new IllegalArgumentException("Request body is empty");
        }
        return objectMapper.readValue(input.getBody(), new TypeReference<>() {});
    }

    private APIGatewayProxyResponseEvent buildResponse(int statusCode, String body) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(Collections.singletonMap("Content-Type", "application/json"))
                .withBody(body);
    }
}
