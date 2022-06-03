package ai.zuva.fields;

import ai.zuva.exception.ZdaiClientException;
import ai.zuva.exception.ZdaiError;
import ai.zuva.http.Response;
import ai.zuva.http.ZdaiHttpClient;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.FileNotFoundException;

public class TrainingRequest {

    public final ZdaiHttpClient client;
    public final String fieldId;
    public final String requestId;

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class TrainingStatus {
        @JsonProperty("field_id")
        public String fieldId;
        public String status;
        @JsonProperty("request_id")
        public String requestId;

        // Expect this to be populated only when status is failed
        @JsonProperty("error")
        public ZdaiError error;
    }

    public TrainingRequest(ZdaiHttpClient client, String fieldId, TrainingExample[] trainingExamples) throws Exception {
        this.client = client;
        this.fieldId = fieldId;

        Response<String> response = client.authorizedRequest("POST",
                String.format("/fields/%s/train", fieldId),
                client.mapper.writeValueAsString(trainingExamples),
                202);

        TrainingStatus status = client.mapper.readValue(response.getBody(), TrainingStatus.class);
        if (status.status.equals("failed")) {
            throw new Exception(String.format("Training failed with error code %s, message: %s", status.error.code, status.error.message));
        }
        this.requestId = status.requestId;
    }

    // Use this constructor to follow up on a request from a previously stored request ID
    public TrainingRequest(ZdaiHttpClient client, String fieldId, String requestId) {
        this.client = client;
        this.fieldId = fieldId;
        this.requestId = requestId;
    }

    public String getStatus() throws Exception {
        Response<String> response = client.authorizedRequest("GET", String.format("/fields/%s/train/%s", fieldId, requestId), 200);
        return client.mapper.readValue(response.getBody(), TrainingStatus.class).status;

    }
}
