package ai.zuva.classification;

import ai.zuva.ProcessingState;
import ai.zuva.exception.ZdaiError;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassificationResult {
    @JsonProperty("file_id")
    public String fileId;
    public ProcessingState status;
    @JsonProperty("request_id")
    public String requestId;
    public String classification;
    @JsonProperty("is_contract")
    public boolean isContract;

    // Expect this to be populated only when status is failed
    @JsonProperty("error")
    public ZdaiError error;
}
