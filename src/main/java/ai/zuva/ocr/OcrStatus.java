package ai.zuva.ocr;

import ai.zuva.RequestStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OcrStatus extends RequestStatus {
    @JsonProperty("file_id")
    public String fileId;
}
