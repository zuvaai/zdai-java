package ai.zuva.docai;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestErrors {
  @JsonProperty("error")
  public RequestError reqError;
}
