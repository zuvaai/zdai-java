package ai.zuva.extraction;

import ai.zuva.RequestStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtractionStatus extends RequestStatus {
    @JsonProperty("file_id")
    public String fileId;

    @JsonProperty("field_ids")
    public String[] fieldIds;
}
