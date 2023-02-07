package ai.zuva.docai.normalization;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DateNormalizationResults extends NormalizationResults {
    @JsonProperty("date")
    public Date[] date;
}
