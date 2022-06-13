package ai.zuva;

import ai.zuva.api.ZdaiApiClient;
import ai.zuva.exception.ZdaiApiException;
import ai.zuva.exception.ZdaiClientException;

public abstract class BaseRequest {
    public final String requestId;
    protected final ZdaiApiClient client;

    protected BaseRequest (ZdaiApiClient client, String requestId) {
        this.client = client;
        this.requestId = requestId;
    }

    public abstract RequestStatus getStatus() throws ZdaiClientException, ZdaiApiException;
}
