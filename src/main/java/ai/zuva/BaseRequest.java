package ai.zuva;

import ai.zuva.api.DocAIClient;
import ai.zuva.exception.DocAIApiException;
import ai.zuva.exception.DocAIClientException;
import ai.zuva.exception.DocAIError;

public abstract class BaseRequest {
    public final String requestId;
    protected final DocAIClient client;

    /**
     * The processing state reported upon creation of the request. For example, it may be "failed"
     * if the file ID to be processed could not be found.
     */
    public final ProcessingState initialStatus;

    /**
     * An error message provided by the API if the initialStatus of the request is "failed."
     */
    public final DocAIError error;

    protected BaseRequest (DocAIClient client, String requestId, ProcessingState initialStatus, DocAIError error) {
        this.client = client;
        this.requestId = requestId;
        this.initialStatus = initialStatus;
        this.error = error;
    }

    protected BaseRequest (DocAIClient client, RequestStatus status) {
        this.client = client;
        this.requestId = status.requestId;
        this.initialStatus = status.status;
        this.error = status.error;
    }

    public abstract RequestStatus getStatus() throws DocAIClientException, DocAIApiException;
}
