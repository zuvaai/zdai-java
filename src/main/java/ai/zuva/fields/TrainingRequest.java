package ai.zuva.fields;

import ai.zuva.exception.ZdaiApiException;
import ai.zuva.exception.ZdaiClientException;
import ai.zuva.exception.ZdaiError;
import ai.zuva.http.Response;
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
     * Construct and send a request training of a field from examples
     * <p>
     * Given a ZdaiHttpClient, a fileId, a field ID, and training examples this
     * constructor makes a request to the Zuva servers to asynchronously train
     * a new version of the field on the specified data. The resulting
     * TrainingRequest object can then be used to check the status of the training
     * process.
     *
     * @param client           The client to use to make the request
     * @param fieldId          The ID of the custom field to train
     * @param trainingExamples The examples to train on
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public TrainingRequest(ZdaiHttpClient client, String fieldId, TrainingExample[] trainingExamples) throws ZdaiClientException, ZdaiApiException {
        this.client = client;
        this.fieldId = fieldId;

        String body;
        try {
            body = client.mapper.writeValueAsString(trainingExamples);
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to create request body", e));
        }

        Response<String> response = client.authorizedRequest("POST",
                String.format("/fields/%s/train", fieldId),
                body,
                202);

        TrainingStatus status = null;
        try {
            status = client.mapper.readValue(response.getBody(), TrainingStatus.class);
            this.requestId = status.requestId;
            this.status = status.status;
            this.error = status.error;
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
        Response<String> response = client.authorizedRequest("GET", String.format("/fields/%s/train/%s", fieldId, requestId), 200);
        try {
            TrainingStatus status = client.mapper.readValue(response.getBody(), TrainingStatus.class);
            this.status = status.status;
            this.error = status.error;
            return this.status;
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to parse response", e));
        }
    }
}
