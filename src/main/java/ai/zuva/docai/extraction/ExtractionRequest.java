package ai.zuva.docai.extraction;

import ai.zuva.docai.BaseRequest;
import ai.zuva.docai.DocAIClient;
import ai.zuva.docai.exception.DocAIApiException;
import ai.zuva.docai.exception.DocAIClientException;
import ai.zuva.docai.files.File;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ExtractionRequest extends BaseRequest {
  public final String fileId;
  public final String[] fieldIds;

  static class ExtractionRequestBody {

    @JsonProperty("file_ids")
    public String[] fileIds;

    @JsonProperty("field_ids")
    public String[] fieldIds;

    public ExtractionRequestBody(File[] files, String[] fieldIds) {
      this.fileIds = File.toFileIdArray(files);
      this.fieldIds = fieldIds;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  static class ExtractionResultsBody {
    @JsonProperty("file_id")
    public String fileId;

    @JsonProperty("request_id")
    public String requestId;

    public ExtractionResults[] results;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  static class ExtractionStatuses {
    @JsonProperty("file_ids")
    public ExtractionStatus[] statuses;
  }

  /**
   * Submits a request to extract fields from a file
   *
   * <p>Given a ZdaiApiClient, a fileId, and an array of field IDs, this constructor makes a request
   * to the Zuva servers to asynchronously extract the specified fields from the file
   *
   * @param client The client to use to make the request
   * @param file The file to analyze
   * @param fieldIds The IDs of the fields to extract from the file
   * @return An ExtractionRequest object, which can be used to check the status and results of the
   *     request
   * @throws DocAIApiException Unsuccessful response code from server
   * @throws DocAIClientException Error preparing, sending or processing the request/response
   */
  public static ExtractionRequest submitRequest(DocAIClient client, File file, String[] fieldIds)
      throws DocAIClientException, DocAIApiException {
    return submitRequests(client, new File[] {file}, fieldIds)[0];
  }

  /**
   * Submits a request to extract fields from one or more files
   *
   * <p>Given a ZdaiApiClient, a fileId, and an array of field IDs, this constructor makes a request
   * to the Zuva servers to asynchronously extract the specified fields from the file
   *
   * @param client The client to use to make the request
   * @param files The files to analyze
   * @param fieldIds The IDs of the fields to extract from the file
   * @return An array of ExtractionRequest objects, which can be used to check the status and
   *     results of the requests
   * @throws DocAIApiException Unsuccessful response code from server
   * @throws DocAIClientException Error preparing, sending or processing the request/response
   */
  public static ExtractionRequest[] submitRequests(
      DocAIClient client, File[] files, String[] fieldIds)
      throws DocAIClientException, DocAIApiException {
    ExtractionStatuses resp =
        client.authorizedJsonRequest(
            "POST",
            "api/v2/extraction",
            new ExtractionRequestBody(files, fieldIds),
            202,
            ExtractionStatuses.class);

    ExtractionRequest[] extractionRequests = new ExtractionRequest[resp.statuses.length];
    for (int i = 0; i < extractionRequests.length; i++) {
      extractionRequests[i] = new ExtractionRequest(client, resp.statuses[i]);
    }
    return extractionRequests;
  }

  // Constructor is private since it is only used by the above static factory methods
  private ExtractionRequest(DocAIClient client, ExtractionStatus extractionStatus) {
    super(client, extractionStatus);
    this.fileId = extractionStatus.fileId;
    this.fieldIds = extractionStatus.fieldIds;
  }

  /**
   * Creates a request object for a pre-existing extraction request.
   *
   * @param client The client to use to make the request
   * @param requestId The ID of an existing extraction request
   */
  public ExtractionRequest(DocAIClient client, String requestId) {
    super(client, requestId, null, null);
    this.fileId = null;
    this.fieldIds = null;
  }

  /**
   * Gets status of the extraction request
   *
   * @return The request status
   * @throws DocAIApiException Unsuccessful response code from server
   * @throws DocAIClientException Error preparing, sending or processing the request/response
   */
  public ExtractionStatus getStatus() throws DocAIClientException, DocAIApiException {
    return client.authorizedGet(
        String.format("api/v2/extraction/%s", requestId), 200, ExtractionStatus.class);
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
   * @return An ExtractionStatus containing the last reported status of the request
   * @throws DocAIClientException Unsuccessful response code from server
   * @throws DocAIApiException Error preparing, sending or processing the request/response
   * @throws InterruptedException Thread interrupted during Thread.sleep()
   */
  public ExtractionStatus pollStatus(long pollingIntervalSeconds, long timeoutSeconds)
      throws DocAIClientException, DocAIApiException, InterruptedException {
    return (ExtractionStatus) super.pollStatus(pollingIntervalSeconds, timeoutSeconds);
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
   * @return An ExtractionStatus containing the last reported status of the request
   * @throws DocAIClientException Unsuccessful response code from server
   * @throws DocAIApiException Error preparing, sending or processing the request/response
   * @throws InterruptedException Thread interrupted during Thread.sleep()
   */
  public ExtractionStatus pollStatus(
      long pollingIntervalSeconds, long timeoutSeconds, boolean showProgress)
      throws DocAIClientException, DocAIApiException, InterruptedException {
    return (ExtractionStatus)
        super.pollStatus(pollingIntervalSeconds, timeoutSeconds, showProgress);
  }

  /**
   * Get results of a successful extraction request
   *
   * <p>Given a ZdaiApiClient, return an array of ExtractionResults containing the text and location
   * of all extractions for each field.
   *
   * @return An array of ExtractionResult objects, containing the results of the extraction
   * @throws DocAIApiException Unsuccessful response code from server
   * @throws DocAIClientException Error preparing, sending or processing the request/response
   */
  public ExtractionResults[] getResults() throws DocAIClientException, DocAIApiException {
    return client.authorizedGet(
            "api/v2/extraction/" + requestId + "/results/text", 200, ExtractionResultsBody.class)
        .results;
  }
}
