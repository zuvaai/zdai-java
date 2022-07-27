package ai.zuva.classification;

import ai.zuva.RequestStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassificationResult extends RequestStatus {
  @JsonProperty("file_id")
  public String fileId;

  public String classification;

  @JsonProperty("is_contract")
  public boolean isContract;
}
