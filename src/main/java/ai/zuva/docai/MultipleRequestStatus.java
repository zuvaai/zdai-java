package ai.zuva.docai;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class MultipleRequestStatus {
  @JsonProperty("num_found")
  public int numFound;

  @JsonProperty("num_errors")
  public int numErrors;

  @JsonProperty("errors")
  public Map<String, RequestErrors> requestErrors;
}
