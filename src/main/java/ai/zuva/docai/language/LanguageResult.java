package ai.zuva.docai.language;

import ai.zuva.docai.RequestStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LanguageResult extends RequestStatus {
  @JsonProperty("file_id")
  public String fileId;

  public String language;
}
