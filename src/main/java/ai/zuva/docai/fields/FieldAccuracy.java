package ai.zuva.docai.fields;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldAccuracy {
  @JsonProperty("f_score")
  public float fScore;

  public float precision;
  public float recall;
}
