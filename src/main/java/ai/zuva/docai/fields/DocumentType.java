package ai.zuva.docai.fields;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DocumentType {
  @JsonProperty("classifications")
  public String[] classifications;

  @JsonProperty("percentage")
  public float percentage;
}
