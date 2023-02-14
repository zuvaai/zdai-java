package ai.zuva.docai.fields;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Languages {
  @JsonProperty("language")
  public String language;

  @JsonProperty("percentage")
  public float percentage;
}
