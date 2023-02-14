package ai.zuva.docai.fields;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Country {
  @JsonProperty("code")
  public String code;

  @JsonProperty("name")
  public String name;
}
