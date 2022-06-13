package ai.zuva.fields;

import ai.zuva.BaseRequest;
import ai.zuva.exception.ZdaiApiException;
import ai.zuva.exception.ZdaiClientException;
import ai.zuva.api.ZdaiApiClient;

public class TrainingRequest extends BaseRequest {

    public final String fieldId;

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
        TrainingStatus trainingStatus  = client.authorizedJsonRequest("POST",
                String.format("api/v2/fields/%s/train", fieldId),
                trainingExamples,
                202,
                TrainingStatus.class);
        return new TrainingRequest(client, trainingStatus);
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
        super(client, requestId, null, null);
        this.fieldId = fieldId;
    }

    private TrainingRequest(ZdaiApiClient client, TrainingStatus trainingStatus) {
        super(client, trainingStatus);
        this.fieldId = trainingStatus.fieldId;
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
        return client.authorizedGet(String.format("api/v2/fields/%s/train/%s", fieldId, requestId), 200, TrainingStatus.class);
    }
}
