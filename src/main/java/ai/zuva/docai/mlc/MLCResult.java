package ai.zuva.docai.mlc;

import ai.zuva.docai.RequestStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MLCResult extends RequestStatus {
    @JsonProperty("file_id")
    public String fileId;

    @JsonProperty("classifications")
    public String[] classifications;
}
