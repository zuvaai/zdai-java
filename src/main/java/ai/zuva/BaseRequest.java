package ai.zuva;

import ai.zuva.exception.DocAIApiException;
import ai.zuva.exception.DocAIClientException;
import ai.zuva.exception.DocAIError;
import java.time.Instant;

public abstract class BaseRequest {
  public final String requestId;
  protected final DocAIClient client;

  /**
   * The processing state reported upon creation of the request. For example, it may be "failed" if
   * the file ID to be processed could not be found.
   */
  public final ProcessingState initialStatus;

  /** An error message provided by the API if the initialStatus of the request is "failed." */
  public final DocAIError error;

  protected BaseRequest(
      DocAIClient client, String requestId, ProcessingState initialStatus, DocAIError error) {
    this.client = client;
    this.requestId = requestId;
    this.initialStatus = initialStatus;
    this.error = error;
  }

  protected BaseRequest(DocAIClient client, RequestStatus status) {
    this.client = client;
    this.requestId = status.requestId;
    this.initialStatus = status.status;
    this.error = status.error;
  }

  public abstract RequestStatus getStatus() throws DocAIClientException, DocAIApiException;

  public RequestStatus pollStatus(long pollingIntervalSeconds, long timeoutSeconds)
      throws DocAIClientException, DocAIApiException, InterruptedException {
    return pollStatus(pollingIntervalSeconds, timeoutSeconds, false);
  }

  public RequestStatus pollStatus(
      long pollingIntervalSeconds, long timeoutSeconds, boolean showProgress)
      throws DocAIClientException, DocAIApiException, InterruptedException {
    if (timeoutSeconds < 1) {
      throw new DocAIClientException("timeoutSeconds must be positive");
    }
    if (pollingIntervalSeconds < 1) {
      throw new DocAIClientException("pollingIntervalSeconds must be positive");
    }
    long tStart = Instant.now().toEpochMilli();
    if (showProgress) System.out.print("Wait for processing");
    while (true) {
      RequestStatus status = this.getStatus();
      if (showProgress) System.out.print(".");
      if (status.isComplete() || status.isFailed()) {
        if (showProgress) System.out.println(status.status.name());
        return status;
      }
      if (Instant.now().toEpochMilli() - tStart > timeoutSeconds * 1000) {
        if (showProgress) {
          System.out.println(
              "Timed out waiting for request to be processed. Last status: "
                  + status.status.toString());
        }
        return status;
      }
      Thread.sleep(pollingIntervalSeconds * 1000);
    }
  }
}
