package ai.zuva.fields;

import ai.zuva.exception.ZdaiApiException;
import ai.zuva.exception.ZdaiClientException;
import ai.zuva.exception.ZdaiError;
import ai.zuva.http.ZdaiHttpClient;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

public class TrainingRequest {

    public final ZdaiHttpClient client;
    public final String fieldId;
    public final String requestId;
    public String status;
    public ZdaiError error;

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

    /**
     * Send a request to train a field from examples
     * <p>
     * Given a ZdaiHttpClient, a fileId, a field ID, and training examples make a request
     * to the Zuva servers to asynchronously train a new version of the field on the specified
     * data. The returned TrainingRequest object can then be used to check the status of the
     * training process.
     *
     * @param client           The client to use to make the request
     * @param fieldId          The ID of the custom field to train
     * @param trainingExamples The examples to train on
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public static TrainingRequest createTrainingRequest(ZdaiHttpClient client, String fieldId, TrainingExample[] trainingExamples) throws ZdaiClientException, ZdaiApiException {
        String body;
        try {
            body = client.mapper.writeValueAsString(trainingExamples);
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to create request body", e));
        }

        String response = client.authorizedRequest("POST",
                String.format("/fields/%s/train", fieldId),
                body,
                202);

        try {
            TrainingStatus trainingStatus = client.mapper.readValue(response, TrainingStatus.class);
            return new TrainingRequest(client, trainingStatus);
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to parse response", e));
        }
    }

    /**
     * Construct a new object representing a pre-existing training request
     * <p>
     * Given a ZdaiHttpClient and a map of file IDs to request IDs, this constructor
     * makes a new ClassificationRequest that can be used to obtain the status and
     * results of the given requests.
     *
     * @param client    The client to use to make the request
     * @param fieldId   The ID of the field being trained
     * @param requestId The ID of an existing request.
     */
    public TrainingRequest(ZdaiHttpClient client, String fieldId, String requestId) {
        this.client = client;
        this.fieldId = fieldId;
        this.requestId = requestId;
    }

    private TrainingRequest(ZdaiHttpClient client, TrainingStatus trainingStatus) {
        this.client = client;
        this.fieldId = trainingStatus.fieldId;
        this.requestId = trainingStatus.requestId;
        this.status = trainingStatus.status;
        this.error = trainingStatus.error;
    }

    /**
     * Get status of a training request from the Zuva server
     * <p>
     * Given a ZdaiHttpClient, return a String indicating the status of the
     * request.
     *
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public String getStatus() throws ZdaiClientException, ZdaiApiException {
        String response = client.authorizedRequest("GET", String.format("/fields/%s/train/%s", fieldId, requestId), 200);
        try {
            TrainingStatus status = client.mapper.readValue(response, TrainingStatus.class);
            this.status = status.status;
            this.error = status.error;
            return this.status;
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to parse response", e));
        }
    }
}
