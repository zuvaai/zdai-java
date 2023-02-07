package ai.zuva.docai.normalization;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Currency {
    @JsonProperty("value")
    public String value;

    @JsonProperty("symbol")
    public String symbol;

    @JsonProperty("precision")
    public int precision;
}
