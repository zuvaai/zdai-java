package ai.zuva.docai.normalization;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CurrencyNormalizationResults extends NormalizationResults {
  @JsonProperty("currency")
  public Currency[] currency;
}
