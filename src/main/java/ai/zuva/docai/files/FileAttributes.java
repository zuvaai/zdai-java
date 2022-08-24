package ai.zuva.docai.files;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FileAttributes {
  @JsonProperty("content-type")
  public String contentType;
}
