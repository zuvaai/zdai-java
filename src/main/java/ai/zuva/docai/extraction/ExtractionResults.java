package ai.zuva.docai.extraction;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExtractionResults {
  @JsonProperty("field_id")
  public String fieldId;

  public ExtractionData[] extractions;
}
