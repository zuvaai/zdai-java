package ai.zuva.docai.normalization;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Duration {
  @JsonProperty("unit")
  public String unit;

  @JsonProperty("value")
  public int value;
}
