package ai.zuva;

import ai.zuva.api.ZdaiApiClient;
import ai.zuva.exception.ZdaiApiException;
import ai.zuva.exception.ZdaiClientException;
import ai.zuva.exception.ZdaiError;

public abstract class BaseRequest {
    public final String requestId;
    protected final ZdaiApiClient client;

    /**
     * The processing state reported upon creation of the request. For example, it may be "failed"
     * if the file ID to be processed could not be found.
     */
    public final ProcessingState initialStatus;

    /**
     * An error message provided by the API if the initialStatus of the request is "failed."
     */
    public final ZdaiError error;

    protected BaseRequest (ZdaiApiClient client, String requestId, ProcessingState initialStatus, ZdaiError error) {
        this.client = client;
        this.requestId = requestId;
        this.initialStatus = initialStatus;
        this.error = error;
    }

    protected BaseRequest (ZdaiApiClient client, RequestStatus status) {
        this.client = client;
        this.requestId = status.requestId;
        this.initialStatus = status.status;
        this.error = status.error;
    }

    public abstract RequestStatus getStatus() throws ZdaiClientException, ZdaiApiException;
}
