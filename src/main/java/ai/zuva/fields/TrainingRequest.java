package ai.zuva.fields;

import ai.zuva.ProcessingState;
import ai.zuva.RequestStatus;
import ai.zuva.exception.ZdaiApiException;
import ai.zuva.exception.ZdaiClientException;
import ai.zuva.exception.ZdaiError;
import ai.zuva.api.ZdaiApiClient;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

public class TrainingRequest {

    public final ZdaiApiClient client;
    public final String fieldId;
    public final String requestId;
    public TrainingStatus status;
    public ZdaiError error;

    /**
     * Send a request to train a field from examples
     * <p>
     * Given a ZdaiApiClient, a fileId, a field ID, and training examples make a request
     * to the Zuva servers to asynchronously train a new version of the field on the specified
     * data. The returned TrainingRequest object can then be used to check the status of the
     * training process.
     *
     * @param client           The client to use to make the request
     * @param fieldId          The ID of the custom field to train
     * @param trainingExamples The examples to train on
     * @return A TrainingRequest object, which can be used to check the status of the request
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public static TrainingRequest createRequest(ZdaiApiClient client, String fieldId, TrainingExample[] trainingExamples) throws ZdaiClientException, ZdaiApiException {
        String response = client.authorizedJsonRequest("POST",
                String.format("/fields/%s/train", fieldId),
                trainingExamples,
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
     * Given a ZdaiApiClient and a map of file IDs to request IDs, this constructor
     * makes a new ClassificationRequest that can be used to obtain the status and
     * results of the given requests.
     *
     * @param client    The client to use to make the request
     * @param fieldId   The ID of the field being trained
     * @param requestId The ID of an existing request.
     */
    public TrainingRequest(ZdaiApiClient client, String fieldId, String requestId) {
        this.client = client;
        this.fieldId = fieldId;
        this.requestId = requestId;
    }

    private TrainingRequest(ZdaiApiClient client, TrainingStatus trainingStatus) {
        this.client = client;
        this.fieldId = trainingStatus.fieldId;
        this.requestId = trainingStatus.requestId;
        this.status = trainingStatus;
    }

    /**
     * Get status of a training request from the Zuva server
     * <p>
     * Given a ZdaiApiClient, make an API request for the status of the training request,
     * returning a TrainingStatus object.
     *
     * @return The request status
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public TrainingStatus getStatus() throws ZdaiClientException, ZdaiApiException {
        String response = client.authorizedGet(String.format("/fields/%s/train/%s", fieldId, requestId), 200);
        try {
            TrainingStatus status = client.mapper.readValue(response, TrainingStatus.class);
            this.status = status;
            return status;
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to parse response", e));
        }
    }
}
