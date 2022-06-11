package ai.zuva.language;

import ai.zuva.ProcessingState;
import ai.zuva.exception.ZdaiError;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LanguageResult {
    @JsonProperty("file_id")
    public String fileId;
    public ProcessingState status;
    @JsonProperty("request_id")
    public String requestId;
    public String language;

    // Expect this to be populated only when status is failed
    @JsonProperty("error")
    public ZdaiError error;
}
