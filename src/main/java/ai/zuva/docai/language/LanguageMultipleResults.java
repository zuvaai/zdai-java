package ai.zuva.docai.language;

import ai.zuva.docai.MultipleRequestStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class LanguageMultipleResults extends MultipleRequestStatus {
  @JsonProperty("statuses")
  public Map<String, LanguageResult> statuses;
}
