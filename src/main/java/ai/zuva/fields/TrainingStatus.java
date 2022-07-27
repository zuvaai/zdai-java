package ai.zuva.fields;

import ai.zuva.RequestStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TrainingStatus extends RequestStatus {
  @JsonProperty("field_id")
  public String fieldId;
}
