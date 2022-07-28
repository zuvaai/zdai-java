package ai.zuva.docai.fields;

import ai.zuva.docai.RequestStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TrainingStatus extends RequestStatus {
  @JsonProperty("field_id")
  public String fieldId;
}
