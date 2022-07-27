package ai.zuva.files;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FileAttributes {
  @JsonProperty("content-type")
  public String contentType;
}
