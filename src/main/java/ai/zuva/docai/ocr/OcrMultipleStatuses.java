package ai.zuva.docai.ocr;

import ai.zuva.docai.MultipleRequestStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class OcrMultipleStatuses extends MultipleRequestStatus {
  @JsonProperty("statuses")
  public Map<String, OcrStatus> statuses;
}
