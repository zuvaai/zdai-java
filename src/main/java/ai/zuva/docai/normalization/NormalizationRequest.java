package ai.zuva.docai.normalization;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NormalizationRequest {
  public String text;

  // This class is used internally to construct the JSON body for the POST /normalization request
  static class NormalizationRequestBody {
    @JsonProperty("text")
    public String text;

    public NormalizationRequestBody(String text) {
      this.text = text;
    }
  }
}
