package ai.zuva.docai.fields;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Jurisdictions {
  @JsonProperty("country")
  public Country[] country;

  @JsonProperty("regions")
  public String[] regions;
}
