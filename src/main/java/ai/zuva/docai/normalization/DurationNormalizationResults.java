package ai.zuva.docai.normalization;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DurationNormalizationResults extends NormalizationResults{
    @JsonProperty("duration")
    public Duration[] duration;
}
