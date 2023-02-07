package ai.zuva.docai.mlc;

import ai.zuva.docai.BaseRequest;
import ai.zuva.docai.DocAIClient;
import ai.zuva.docai.exception.DocAIApiException;
import ai.zuva.docai.exception.DocAIClientException;
import ai.zuva.docai.files.File;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MLCRequest extends BaseRequest {
  public final String fileId;
  public static List<String> mlcRequestIds = new ArrayList<>();

  // This class is used internally to construct the JSON body for the POST /mlc request
  static class MLCRequestBody {
    @JsonProperty("file_ids")
    public String[] fileIds;

    public MLCRequestBody(File[] files) {
      this.fileIds = File.toFileIdArray(files);
    }
  }

  // This class is used internally to read the JSON body from the POST /mlc response
  static class MLCResultsBody {
    @JsonProperty("file_ids")
    public MLCResult[] results;
  }

  /**
   * Sends a request to classify the file using the multi-level classification service.
   *
   * <p>Given a ZdaiApiClient and a ZdaiFile, make a request to the Zuva servers to asynchronously
   * classify the multi-level classification of the file. The created MLCRequest object can then be
   * used to query the status and results of the request.
   *
   * @param client The client to use to make the request
   * @param file The file to classify
   * @return A MLCRequest, which can be used to check the status and results of the request
   * @throws DocAIClientException Unsuccessful response code from server
   * @throws DocAIApiException Error preparing, sending or processing the request/response
   */
  public static MLCRequest createRequest(DocAIClient client, File file)
      throws DocAIClientException, DocAIApiException {
    return createRequests(client, new File[] {file})[0];
  }

  /**
   * Sends a request to classify multiple files using multi-level classification service.
   *
   * <p>Given a ZdaiApiClient and an array of ZdaiFiles, make a request to the Zuva servers to
   * asynchronously classify the multi-level classification of the files. The created MLCRequest
   * objects can then be used to query the status and results of each request.
   *
   * @param client The client to use to make the request
   * @param files The files to classify
   * @return An array of MLCRequests, which can be used to check the status and results of the
   *     requests
   * @throws DocAIClientException Unsuccessful response code from server
   * @throws DocAIApiException Error preparing, sending or processing the request/response
   */
  public static MLCRequest[] createRequests(DocAIClient client, File[] files)
      throws DocAIClientException, DocAIApiException {
    MLCResultsBody resp =
        client.authorizedJsonRequest(
            "POST", "api/v2/mlc", new MLCRequestBody(files), 202, MLCResultsBody.class);

    MLCRequest[] mlcRequests = new MLCRequest[resp.results.length];
    for (int i = 0; i < mlcRequests.length; i++) {
      mlcRequests[i] = new MLCRequest(client, resp.results[i]);
      mlcRequestIds.add(mlcRequests[i].requestId);
    }
    return mlcRequests;
  }

  private MLCRequest(DocAIClient client, MLCResult result) {
    super(client, result);
    this.fileId = result.fileId;
  }

  /**
   * Constructs a new object representing a pre-existing mlc request
   *
   * <p>Given a ZdaiApiClient and a map of file IDs to request IDs, this constructor makes a new
   * MLCRequest that can be used to obtain the status and results of the given request.
   *
   * @param client The client to use to make the request
   * @param fileId The ID of the file being classified
   * @param requestID The ID of an existing request.
   */
  public MLCRequest(DocAIClient client, String fileId, String requestID) {
    super(client, requestID, null, null);
    this.fileId = fileId;
  }

  /**
   * Get mlc status and results.
   *
   * <p>Given a ZdaiApiClient, return a MLCResult indicating the status of the MLC request for that
   * file and the MLC result (if available).
   *
   * @return A MLCResult, with the status and results of the request
   * @throws DocAIClientException Unsuccessful response code from server
   * @throws DocAIApiException Error preparing, sending or processing the request/response
   */
  public MLCResult getStatus() throws DocAIClientException, DocAIApiException {
    return client.authorizedGet("api/v2/mlc/" + requestId, 200, MLCResult.class);
  }

  /**
   * Get multiple MLC statuses and results
   *
   * @return A MLCMultipleResults object, containing the statuses of all requests and, if available,
   *     the results
   * @throws DocAIClientException Unsuccessful response code from server
   * @throws DocAIApiException Error preparing, sending or processing the request/response
   */
  public MLCMultipleResults getStatuses() throws DocAIClientException, DocAIApiException {
    Map<String, String> queryParamsMap = client.listToMapQueryParams(mlcRequestIds);
    return client.authorizedGet(
        "api/v2/mlcs&" + client.mapToQueryParams(queryParamsMap), 200, MLCMultipleResults.class);
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
   * @return A MLCResult, with the last reported status and results of the request
   * @throws DocAIClientException Unsuccessful response code from server
   * @throws DocAIApiException Error preparing, sending or processing the request/response
   * @throws InterruptedException Thread interrupted during Thread.sleep()
   */
  public MLCResult pollStatus(long pollingIntervalSeconds, long timeoutSeconds)
      throws DocAIClientException, DocAIApiException, InterruptedException {
    return (MLCResult) super.pollStatus(pollingIntervalSeconds, timeoutSeconds);
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
   * @return A MLCResult, with the last reported status and results of the request
   * @throws DocAIClientException Unsuccessful response code from server
   * @throws DocAIApiException Error preparing, sending or processing the request/response
   * @throws InterruptedException Thread interrupted during Thread.sleep()
   */
  public MLCResult pollStatus(
      long pollingIntervalSeconds, long timeoutSeconds, boolean showProgress)
      throws DocAIClientException, DocAIApiException, InterruptedException {
    return (MLCResult) super.pollStatus(pollingIntervalSeconds, timeoutSeconds, showProgress);
  }
}
