package ai.zuva.docai.normalization;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Date {
  @JsonProperty("day")
  public int day;

  @JsonProperty("month")
  public int month;

  @JsonProperty("year")
  public int year;
}
