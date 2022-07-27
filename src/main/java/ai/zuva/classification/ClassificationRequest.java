package ai.zuva.classification;

import ai.zuva.BaseRequest;
import ai.zuva.api.DocAIClient;
import ai.zuva.exception.DocAIApiException;
import ai.zuva.exception.DocAIClientException;
import ai.zuva.files.File;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ClassificationRequest extends BaseRequest {
  public final String fileId;

  // This class is used internally to construct the JSON body for the POST /classification request
  static class ClassificationRequestBody {
    @JsonProperty("file_ids")
    public String[] fileIds;

    public ClassificationRequestBody(File[] files) {
      this.fileIds = File.toFileIdArray(files);
    }
  }

  // This class is used internally to read the JSON body from the POST /classification request
  static class ClassificationResultsBody {
    @JsonProperty("file_ids")
    public ClassificationResult[] results;
  }

  /**
   * Send a request to classify the document type of a file.
   *
   * <p>Given a ZdaiApiClient and a ZdaiFile, make a request to the Zuva servers to asynchronously
   * classify the document type of the file. The created ClassificationRequest object can then be
   * used to query the status and results of the request.
   *
   * @param client The client to use to make the request
   * @param file The file to classify
   * @return A ClassificationRequest, which can be used to check the status and results of the
   *     request
   * @throws DocAIApiException Unsuccessful response code from server
   * @throws DocAIClientException Error preparing, sending or processing the request/response
   */
  public static ClassificationRequest createRequest(DocAIClient client, File file)
      throws DocAIClientException, DocAIApiException {
    return createRequests(client, new File[] {file})[0];
  }

  /**
   * Send a request to classify the document type of multiple files.
   *
   * <p>Given a ZdaiApiClient and an array of ZdaiFiles, make a request to the Zuva servers to
   * asynchronously classify the document type of the files. The created ClassificationRequests
   * objects can then be used to query the status and results of each request.
   *
   * @param client The client to use to make the request
   * @param files The files to classify
   * @return An array of ClassificationRequests, which can be used to check the status and results
   *     of the requests
   * @throws DocAIApiException Unsuccessful response code from server
   * @throws DocAIClientException Error preparing, sending or processing the request/response
   */
  public static ClassificationRequest[] createRequests(DocAIClient client, File[] files)
      throws DocAIClientException, DocAIApiException {
    ClassificationResultsBody resp =
        client.authorizedJsonRequest(
            "POST",
            "api/v2/classification",
            new ClassificationRequestBody(files),
            202,
            ClassificationResultsBody.class);

    ClassificationRequest[] classificationRequests = new ClassificationRequest[resp.results.length];
    for (int i = 0; i < classificationRequests.length; i++) {
      classificationRequests[i] = new ClassificationRequest(client, resp.results[i]);
    }
    return classificationRequests;
  }

  private ClassificationRequest(DocAIClient client, ClassificationResult result) {
    super(client, result);
    this.fileId = result.fileId;
  }

  /**
   * Construct a new object representing a pre-existing classification request
   *
   * <p>Given a ZdaiApiClient and a map of file IDs to request IDs, this constructor makes a new
   * ClassificationRequest that can be used to obtain the status and results of the given request.
   *
   * @param client The client to use to make the request
   * @param fileId The ID of the file being classified
   * @param requestId The ID of an existing request.
   */
  public ClassificationRequest(DocAIClient client, String fileId, String requestId) {
    super(client, requestId, null, null);
    this.fileId = fileId;
  }

  /**
   * Get classification status and results from the Zuva server
   *
   * <p>Given a ZdaiApiClient, return a ClassificationResult indicating the status of the
   * classification request for that file and the classification result (if available).
   *
   * @return A ClassificationResult, with the status and results of the request
   * @throws DocAIApiException Unsuccessful response code from server
   * @throws DocAIClientException Error preparing, sending or processing the request/response
   */
  public ClassificationResult fetchStatus() throws DocAIClientException, DocAIApiException {
    return client.authorizedGet(
        "api/v2/classification/" + requestId, 200, ClassificationResult.class);
  }

  /**
   * @param pollingIntervalSeconds The time in seconds to wait between status requests
   * @param timeoutSeconds The time in seconds to wait for a complete (or failed) status before
   *     timing out the operation
   * @return A ClassificationResult, with the status and results of the request
   * @throws DocAIClientException Unsuccessful response code from server
   * @throws DocAIApiException Error preparing, sending or processing the request/response
   * @throws InterruptedException Thread interrupted during Thread.sleep()
   */
  public ClassificationResult waitUntilFinished(long pollingIntervalSeconds, long timeoutSeconds)
      throws DocAIClientException, DocAIApiException, InterruptedException {
    return (ClassificationResult) super.waitUntilFinished(pollingIntervalSeconds, timeoutSeconds);
  }

  /**
   * @param pollingIntervalSeconds The time in seconds to wait between status requests
   * @param timeoutSeconds The time in seconds to wait for a complete (or failed) status before
   *     timing out the operation
   * @param showProgress Flag indicating whether to print a progress indicator while waiting for
   *     completion
   * @return A ClassificationResult, with the status and results of the request
   * @throws DocAIClientException Unsuccessful response code from server
   * @throws DocAIApiException Error preparing, sending or processing the request/response
   * @throws InterruptedException Thread interrupted during Thread.sleep()
   */
  public ClassificationResult waitUntilFinished(
      long pollingIntervalSeconds, long timeoutSeconds, boolean showProgress)
      throws DocAIClientException, DocAIApiException, InterruptedException {
    return (ClassificationResult)
        super.waitUntilFinished(pollingIntervalSeconds, timeoutSeconds, showProgress);
  }
}
