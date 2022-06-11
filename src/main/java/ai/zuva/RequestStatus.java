package ai.zuva;

import ai.zuva.exception.ZdaiError;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestStatus {
    @JsonProperty("request_id")
    public String requestId;

    public ProcessingState status;

    // Expect this to be populated only when status is failed
    @JsonProperty("error")
    public ZdaiError error;

    public boolean isQueued() {
        return status.isQueued();
    }

    public boolean isProcessing() {
        return status.isProcessing();
    }

    public boolean isComplete() {
        return status.isComplete();
    }

    public boolean isFailed() {
        return status.isFailed();
    }
}
