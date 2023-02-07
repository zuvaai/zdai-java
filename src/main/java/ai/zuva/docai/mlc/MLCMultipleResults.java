package ai.zuva.docai.mlc;

import ai.zuva.docai.MultipleRequestStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class MLCMultipleResults extends MultipleRequestStatus {
  @JsonProperty("statuses")
  public Map<String, MLCResult> statuses;
}
