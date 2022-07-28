package ai.zuva.docai.fields;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldValidation {
  @JsonProperty("file_id")
  public String fileId;

  public String type;
  public int[] location;
}
