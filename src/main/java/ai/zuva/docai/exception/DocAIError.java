package ai.zuva.docai.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DocAIError {
  public String code;
  public String message;
}
