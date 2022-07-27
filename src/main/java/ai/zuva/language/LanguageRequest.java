package ai.zuva.language;

import ai.zuva.BaseRequest;
import ai.zuva.api.DocAIClient;
import ai.zuva.exception.DocAIApiException;
import ai.zuva.exception.DocAIClientException;
import ai.zuva.files.File;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LanguageRequest extends BaseRequest {
  public final String fileId;

  // This class is used internally to construct the JSON body for the POST /language request
  static class LanguageRequestBody {
    @JsonProperty("file_ids")
    public String[] fileIds;

    public LanguageRequestBody(File[] files) {
      this.fileIds = File.toFileIdArray(files);
    }
  }

  // This class is used internally to read the JSON body from the POST /language request
  static class LanguageResults {
    @JsonProperty("file_ids")
    public LanguageResult[] results;
  }

  /**
   * Send a request to classify the document's primary language.
   *
   * <p>Given a ZdaiApiClient and a fileId, make a request to the Zuva API to asynchronously
   * classify the language of the file. The created LanguageRequest object can then be used to query
   * the status and results of the request.
   *
   * @param client The client to use to make the request
   * @param file The file to classify
   * @return A LanguageRequest object, which can be used to check the status and results of the
   *     request
   * @throws DocAIApiException Unsuccessful response code from server
   * @throws DocAIClientException Error preparing, sending or processing the request/response
   */
  public static LanguageRequest createRequest(DocAIClient client, File file)
      throws DocAIClientException, DocAIApiException {
    return createRequests(client, new File[] {file})[0];
  }

  /**
   * Send a request to classify the primary language of multiple files
   *
   * <p>Given a ZdaiApiClient and a fileId, make a request to the Zuva API to asynchronously
   * classify the language of the file. The created LanguageRequest object can then be used to query
   * the status and results of the request.
   *
   * @param client The client to use to make the request
   * @param files The files to classify
   * @return An array of LanguageRequest objects, which can be used to check the status and results
   *     of the requests
   * @throws DocAIApiException Unsuccessful response code from server
   * @throws DocAIClientException Error preparing, sending or processing the request/response
   */
  public static LanguageRequest[] createRequests(DocAIClient client, File[] files)
      throws DocAIClientException, DocAIApiException {
    LanguageResults resp =
        client.authorizedJsonRequest(
            "POST", "api/v2/language", new LanguageRequestBody(files), 202, LanguageResults.class);
    LanguageRequest[] languageRequests = new LanguageRequest[resp.results.length];
    for (int i = 0; i < languageRequests.length; i++) {
      languageRequests[i] =
          new LanguageRequest(client, resp.results[i].fileId, resp.results[i].requestId);
    }
    return languageRequests;
  }

  private LanguageRequest(DocAIClient client, LanguageResult result) {
    super(client, result);
    this.fileId = result.fileId;
  }

  /**
   * Construct a new object representing a pre-existing language request
   *
   * <p>Given a ZdaiApiClient, a file ID and a request ID, construct a new LanguageRequest object
   * that can be used to obtain the status and results of the given request.
   *
   * @param client The client to use to make the request
   * @param fileId The file ID of the existing request
   * @param requestId The request ID of the existing request
   */
  public LanguageRequest(DocAIClient client, String fileId, String requestId) {
    super(client, requestId, null, null);
    this.fileId = fileId;
  }

  /**
   * Get language status and results from the Zuva server
   *
   * <p>Given a ZdaiApiClient, return a LanguageResult indicating the status of the request and the
   * language result (if available).
   *
   * @return A LanguageResult object, containing both the status of the request and, if available,
   *     the results.
   * @throws DocAIApiException Unsuccessful response code from server
   * @throws DocAIClientException Error preparing, sending or processing the request/response
   */
  public LanguageResult fetchStatus() throws DocAIClientException, DocAIApiException {
    return client.authorizedGet("api/v2/language/" + requestId, 200, LanguageResult.class);
  }

  /**
   * @param pollingIntervalSeconds The time in seconds to wait between status requests
   * @param timeoutSeconds The time in seconds to wait for a complete (or failed) status before
   *     timing out the operation
   * @return A LanguageResult, with the status and results of the request
   * @throws DocAIClientException Unsuccessful response code from server
   * @throws DocAIApiException Error preparing, sending or processing the request/response
   * @throws InterruptedException Thread interrupted during Thread.sleep()
   */
  public LanguageResult waitUntilFinished(long pollingIntervalSeconds, long timeoutSeconds)
      throws DocAIClientException, DocAIApiException, InterruptedException {
    return (LanguageResult) super.waitUntilFinished(pollingIntervalSeconds, timeoutSeconds);
  }

  /**
   * @param pollingIntervalSeconds The time in seconds to wait between status requests
   * @param timeoutSeconds The time in seconds to wait for a complete (or failed) status before
   *     timing out the operation
   * @param showProgress Flag indicating whether to print a progress indicator while waiting for
   *     completion
   * @return A LanguageResult, with the status and results of the request
   * @throws DocAIClientException Unsuccessful response code from server
   * @throws DocAIApiException Error preparing, sending or processing the request/response
   * @throws InterruptedException Thread interrupted during Thread.sleep()
   */
  public LanguageResult waitUntilFinished(
      long pollingIntervalSeconds, long timeoutSeconds, boolean showProgress)
      throws DocAIClientException, DocAIApiException, InterruptedException {
    return (LanguageResult)
        super.waitUntilFinished(pollingIntervalSeconds, timeoutSeconds, showProgress);
  }
}
