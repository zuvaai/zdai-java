package ai.zuva.docai.ocr;

import static ai.zuva.docai.DocAIClient.listToMapQueryParams;
import static ai.zuva.docai.DocAIClient.mapToQueryParams;

import ai.zuva.docai.BaseRequest;
import ai.zuva.docai.DocAIClient;
import ai.zuva.docai.exception.DocAIApiException;
import ai.zuva.docai.exception.DocAIClientException;
import ai.zuva.docai.files.File;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OcrRequest extends BaseRequest {
  public final String fileId;

  @JsonIgnoreProperties(ignoreUnknown = true)
  static class OcrStatuses {
    @JsonProperty("file_ids")
    public OcrStatus[] statuses;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  static class OcrText {
    @JsonProperty("request_id")
    public String requestId;

    public String text;
  }

  static class OcrRequestBody {
    @JsonProperty("file_ids")
    public String[] fileIds;

    public OcrRequestBody(File[] files) {
      this.fileIds = File.toFileIdArray(files);
    }
  }

  /**
   * Send a request to perform OCR on a file
   *
   * <p>Given a ZdaiApiClient, a fileId, makes a request to the Zuva servers to asynchronously
   * perform OCR on the file, returning an OcrRequest object that can subsequently be used to obtain
   * the file's text, images and layouts.
   *
   * @param client The client to use to make the request
   * @param file The file to analyze
   * @return An OcrRequest object, which can be used to check the status and results of the request
   * @throws DocAIApiException Unsuccessful response code from server
   * @throws DocAIClientException Error preparing, sending or processing the request/response
   */
  public static OcrRequest createRequest(DocAIClient client, File file)
      throws DocAIClientException, DocAIApiException {
    return createRequests(client, new File[] {file})[0];
  }

  /**
   * Send a request to extract fields from multiple files
   *
   * <p>Given a ZdaiApiClient, a fileId, makes a request to the Zuva servers to asynchronously
   * perform OCR on the file, returning an OcrRequest object that can subsequently be used to obtain
   * the file's text, images and layouts.
   *
   * @param client The client to use to make the request
   * @param files The files to analyze
   * @return An array of OcrRequest objects, which can be used to check the status and results of
   *     the requests
   * @throws DocAIApiException Unsuccessful response code from server
   * @throws DocAIClientException Error preparing, sending or processing the request/response
   */
  public static OcrRequest[] createRequests(DocAIClient client, File[] files)
      throws DocAIClientException, DocAIApiException {
    OcrStatuses resp =
        client.authorizedJsonRequest(
            "POST", "api/v2/ocr", new OcrRequestBody(files), 202, OcrStatuses.class);
    OcrRequest[] ocrRequests = new OcrRequest[resp.statuses.length];
    for (int i = 0; i < ocrRequests.length; i++) {
      ocrRequests[i] = new OcrRequest(client, resp.statuses[i]);
    }
    return ocrRequests;
  }

  private OcrRequest(DocAIClient client, OcrStatus status) {
    super(client, status);
    this.fileId = status.fileId;
  }

  public OcrRequest(DocAIClient client, String requestId) {
    super(client, requestId, null, null);
    this.fileId = null;
  }

  /**
   * Gets status of an OCR request
   *
   * <p>Given a ZdaiApiClient, return a String indicating the status of the request.
   *
   * @return The request status as a String (one of "queued", "processing", "complete" or "failed")
   * @throws DocAIApiException Unsuccessful response code from server
   * @throws DocAIClientException Error preparing, sending or processing the request/response
   */
  public OcrStatus getStatus() throws DocAIClientException, DocAIApiException {
    return client.authorizedGet("api/v2/ocr/" + requestId, 200, OcrStatus.class);
  }

  /**
   * Get multiple ocr statuses
   *
   * @param client The client to use to make the request
   * @param ocrRequests array of OcrRequest objects
   * @return An OcrMultipleStatuses object, containing the statuses of all requests
   * @throws DocAIClientException Unsuccessful response code from server
   * @throws DocAIApiException Error preparing, sending or processing the request/response
   */
  public static OcrMultipleStatuses getStatuses(DocAIClient client, OcrRequest[] ocrRequests)
      throws DocAIClientException, DocAIApiException {
    List<String> ocrRequestIds = new ArrayList<>();
    for (OcrRequest request : ocrRequests) {
      ocrRequestIds.add(request.requestId);
    }
    return getStatuses(client, ocrRequestIds);
  }

  /**
   * Get multiple ocr statuses
   *
   * @param client The client to use to make the request
   * @param ocrRequestIds list of OCR Request IDs
   * @return An OcrMultipleStatuses object, containing the statuses of all requests
   * @throws DocAIClientException Unsuccessful response code from server
   * @throws DocAIApiException Error preparing, sending or processing the request/response
   */
  public static OcrMultipleStatuses getStatuses(DocAIClient client, List<String> ocrRequestIds)
      throws DocAIClientException, DocAIApiException {
    Map<String, String> queryParamsMap = listToMapQueryParams("request_id", ocrRequestIds);
    return client.authorizedGet(
        "api/v2/ocrs&" + mapToQueryParams(queryParamsMap), 200, OcrMultipleStatuses.class);
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
   * @return A OcrStatus, with the last reported status of the request
   * @throws DocAIClientException Unsuccessful response code from server
   * @throws DocAIApiException Error preparing, sending or processing the request/response
   * @throws InterruptedException Thread interrupted during Thread.sleep()
   */
  public OcrStatus pollStatus(long pollingIntervalSeconds, long timeoutSeconds)
      throws DocAIClientException, DocAIApiException, InterruptedException {
    return (OcrStatus) super.pollStatus(pollingIntervalSeconds, timeoutSeconds);
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
   * @return A OcrStatus, with the last reported status of the request
   * @throws DocAIClientException Unsuccessful response code from server
   * @throws DocAIApiException Error preparing, sending or processing the request/response
   * @throws InterruptedException Thread interrupted during Thread.sleep()
   */
  public OcrStatus pollStatus(
      long pollingIntervalSeconds, long timeoutSeconds, boolean showProgress)
      throws DocAIClientException, DocAIApiException, InterruptedException {
    return (OcrStatus) super.pollStatus(pollingIntervalSeconds, timeoutSeconds, showProgress);
  }

  /**
   * Gets text results of an OCR request
   *
   * <p>Given a ZdaiApiClient, return the OCR text of the document as a String.
   *
   * @return The text of the document as a String
   * @throws DocAIApiException Unsuccessful response code from server
   * @throws DocAIClientException Error preparing, sending or processing the request/response
   */
  public String getText() throws DocAIClientException, DocAIApiException {
    return client.authorizedGet("api/v2/ocr/" + requestId + "/text", 200, OcrText.class).text;
  }

  /**
   * Gets image results of an OCR request
   *
   * @return A byte array of the OCR images of the document as a zip file containing a PNG image of
   *     each page.
   * @throws DocAIApiException Unsuccessful response code from server
   * @throws DocAIClientException Error preparing, sending or processing the request/response
   */
  public byte[] getImages() throws DocAIClientException, DocAIApiException {
    return client.authorizedGetBinary("api/v2/ocr/" + requestId + "/images", 200);
  }

  /**
   * Gets layout results of an OCR request
   *
   * @return The layout of the document in a protobuff format, as a byte array
   * @throws DocAIApiException Unsuccessful response code from server
   * @throws DocAIClientException Error preparing, sending or processing the request/response
   */
  public byte[] getLayouts() throws DocAIClientException, DocAIApiException {
    return client.authorizedGetBinary("api/v2/ocr/" + requestId + "/layouts", 200);
  }
}
