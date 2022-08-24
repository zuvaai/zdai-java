package ai.zuva.docai.fields;

import ai.zuva.docai.BaseRequest;
import ai.zuva.docai.DocAIClient;
import ai.zuva.docai.exception.DocAIApiException;
import ai.zuva.docai.exception.DocAIClientException;

public class TrainingRequest extends BaseRequest {

  public final String fieldId;

  /**
   * Sends a request to train a field from examples
   *
   * <p>Given a ZdaiApiClient, a file ID, a field ID, and training examples make a request to
   * asynchronously train a new version of the field on the specified data. The returned
   * TrainingRequest object can then be used to check the status of the training process.
   *
   * @param client The client to use to make the request
   * @param fieldId The ID of the custom field to train
   * @param trainingExamples The examples to train on
   * @return A TrainingRequest object, which can be used to check the status of the request
   * @throws DocAIApiException Unsuccessful response code from server
   * @throws DocAIClientException Error preparing, sending or processing the request/response
   */
  public static TrainingRequest createRequest(
      DocAIClient client, String fieldId, TrainingExample[] trainingExamples)
      throws DocAIClientException, DocAIApiException {
    TrainingStatus trainingStatus =
        client.authorizedJsonRequest(
            "POST",
            String.format("api/v2/fields/%s/train", fieldId),
            trainingExamples,
            202,
            TrainingStatus.class);
    return new TrainingRequest(client, trainingStatus);
  }

  /**
   * Constructs a new object representing a pre-existing training request
   *
   * @param client The client to use to make the request
   * @param fieldId The ID of the field being trained
   * @param requestId The ID of an existing request.
   */
  public TrainingRequest(DocAIClient client, String fieldId, String requestId) {
    super(client, requestId, null, null);
    this.fieldId = fieldId;
  }

  private TrainingRequest(DocAIClient client, TrainingStatus trainingStatus) {
    super(client, trainingStatus);
    this.fieldId = trainingStatus.fieldId;
  }

  /**
   * Gets status of a training request
   *
   * <p>Given a ZdaiApiClient, make an API request for the status of the training request, returning
   * a TrainingStatus object.
   *
   * @return The request status
   * @throws DocAIApiException Unsuccessful response code from server
   * @throws DocAIClientException Error preparing, sending or processing the request/response
   */
  public TrainingStatus getStatus() throws DocAIClientException, DocAIApiException {
    return client.authorizedGet(
        String.format("api/v2/fields/%s/train/%s", fieldId, requestId), 200, TrainingStatus.class);
  }

  /**
   * Blocks until the request completes or fails, or the specified timeout is reached
   *
   * <p>Given a polling interval and timeout in second, polls the request status until it reaches a
   * terminal state or the specified timeout, at which point it returns the result of the most
   * recent status request.
   *
   * @param pollingIntervalSeconds The time in seconds to wait between status requests
   * @param timeoutSeconds The time in seconds to wait for a complete (or failed) status before
   *     timing out the operation
   * @return A TrainingStatus, with the last reported status of the request
   * @throws DocAIClientException Unsuccessful response code from server
   * @throws DocAIApiException Error preparing, sending or processing the request/response
   * @throws InterruptedException Thread interrupted during Thread.sleep()
   */
  public TrainingStatus pollStatus(long pollingIntervalSeconds, long timeoutSeconds)
      throws DocAIClientException, DocAIApiException, InterruptedException {
    return (TrainingStatus) super.pollStatus(pollingIntervalSeconds, timeoutSeconds);
  }

  /**
   * Blocks until the request completes or fails, or the specified timeout is reached
   *
   * <p>Given a polling interval and timeout in second, polls the request status until it reaches a
   * terminal state or the specified timeout, at which point it returns the result of the most
   * recent status request.
   *
   * @param pollingIntervalSeconds The time in seconds to wait between status requests
   * @param timeoutSeconds The time in seconds to wait for a complete (or failed) status before
   *     timing out the operation
   * @param showProgress Flag indicating whether to print a progress indicator while waiting for
   *     completion
   * @return A TrainingStatus, with the last reported status of the request
   * @throws DocAIClientException Unsuccessful response code from server
   * @throws DocAIApiException Error preparing, sending or processing the request/response
   * @throws InterruptedException Thread interrupted during Thread.sleep()
   */
  public TrainingStatus pollStatus(
      long pollingIntervalSeconds, long timeoutSeconds, boolean showProgress)
      throws DocAIClientException, DocAIApiException, InterruptedException {
    return (TrainingStatus) super.pollStatus(pollingIntervalSeconds, timeoutSeconds, showProgress);
  }
}
