package ai.zuva.docai.fields;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Location {
  @JsonProperty("start")
  public long start;

  @JsonProperty("end")
  public long end;

  // Default constructor necessary for JSON deserialization
  public Location() {}

  public Location(long start, long end) {
    this.start = start;
    this.end = end;
  }
}
