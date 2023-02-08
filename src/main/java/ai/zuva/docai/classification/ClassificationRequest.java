package ai.zuva.docai.classification;

import ai.zuva.docai.BaseRequest;
import ai.zuva.docai.DocAIClient;
import ai.zuva.docai.exception.DocAIApiException;
import ai.zuva.docai.exception.DocAIClientException;
import ai.zuva.docai.files.File;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClassificationRequest extends BaseRequest {
  public final String fileId;
  public static List<String> classificationRequestIds = new ArrayList<>();

  // This class is used internally to construct the JSON body for the POST /classification request
  static class ClassificationRequestBody {
    @JsonProperty("file_ids")
    public String[] fileIds;

    public ClassificationRequestBody(File[] files) {
      this.fileIds = File.toFileIdArray(files);
    }
  }

  // This class is used internally to read the JSON body from the POST /classification response
  static class ClassificationResultsBody {
    @JsonProperty("file_ids")
    public ClassificationResult[] results;
  }

  /**
   * Sends a request to classify the file by document type.
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
   * Sends a request to classify multiple files by document type.
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
      classificationRequestIds.add(classificationRequests[i].requestId);
    }
    return classificationRequests;
  }

  private ClassificationRequest(DocAIClient client, ClassificationResult result) {
    super(client, result);
    this.fileId = result.fileId;
  }

  /**
   * Constructs a new object representing a pre-existing classification request
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
   * Get classification status and results.
   *
   * <p>Given a ZdaiApiClient, return a ClassificationResult indicating the status of the
   * classification request for that file and the classification result (if available).
   *
   * @return A ClassificationResult, with the status and results of the request
   * @throws DocAIApiException Unsuccessful response code from server
   * @throws DocAIClientException Error preparing, sending or processing the request/response
   */
  public ClassificationResult getStatus() throws DocAIClientException, DocAIApiException {
    return client.authorizedGet(
        "api/v2/classification/" + requestId, 200, ClassificationResult.class);
  }

  /**
   * Get multiple classification statuses and results
   *
   * @return A ClassificationMultipleResults object, containing the statuses of all requests and, if
   *     available, the results
   * @throws DocAIClientException Unsuccessful response code from server
   * @throws DocAIApiException Error preparing, sending or processing the request/response
   */
  public ClassificationMultipleResults getStatuses()
      throws DocAIClientException, DocAIApiException {
    Map<String, String> queryParamsMap = client.listToMapQueryParams("request_id", classificationRequestIds);
    return client.authorizedGet(
        "api/v2/classifications&" + client.mapToQueryParams(queryParamsMap),
        200,
        ClassificationMultipleResults.class);
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
   * @return A ClassificationResult, with the last reported status and results of the request
   * @throws DocAIClientException Unsuccessful response code from server
   * @throws DocAIApiException Error preparing, sending or processing the request/response
   * @throws InterruptedException Thread interrupted during Thread.sleep()
   */
  public ClassificationResult pollStatus(long pollingIntervalSeconds, long timeoutSeconds)
      throws DocAIClientException, DocAIApiException, InterruptedException {
    return (ClassificationResult) super.pollStatus(pollingIntervalSeconds, timeoutSeconds);
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
   * @return A ClassificationResult, with the last reported status and results of the request
   * @throws DocAIClientException Unsuccessful response code from server
   * @throws DocAIApiException Error preparing, sending or processing the request/response
   * @throws InterruptedException Thread interrupted during Thread.sleep()
   */
  public ClassificationResult pollStatus(
      long pollingIntervalSeconds, long timeoutSeconds, boolean showProgress)
      throws DocAIClientException, DocAIApiException, InterruptedException {
    return (ClassificationResult)
        super.pollStatus(pollingIntervalSeconds, timeoutSeconds, showProgress);
  }
}
