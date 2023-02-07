package ai.zuva.docai.classification;

import ai.zuva.docai.MultipleRequestStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class ClassificationMultipleResults extends MultipleRequestStatus {
  @JsonProperty("statuses")
  public Map<String, ClassificationResult> statuses;
}
