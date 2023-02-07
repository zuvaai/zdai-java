package ai.zuva.docai;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestError {
  @JsonProperty("code")
  public String code;

  @JsonProperty("message")
  public String message;
}
