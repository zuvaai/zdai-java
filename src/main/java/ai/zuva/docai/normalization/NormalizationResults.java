package ai.zuva.docai.normalization;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NormalizationResults {
    @JsonProperty("request_id")
    public String requestID;

    @JsonProperty("text")
    public String text;

    @JsonProperty("sha-256")
    public String sha;
}
