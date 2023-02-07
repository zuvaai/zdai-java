package ai.zuva.docai.extraction;

import ai.zuva.docai.MultipleRequestStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class ExtractionMultipleStatuses extends MultipleRequestStatus {
  @JsonProperty("statuses")
  public Map<String, ExtractionStatus> statuses;
}
