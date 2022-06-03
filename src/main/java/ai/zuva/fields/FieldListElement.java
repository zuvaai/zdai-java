package ai.zuva.fields;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldListElement {
    @JsonProperty("field_id")
    public String fieldId;

    public String name;
    public String description;

    @JsonProperty("document_count")
    public int documentCount;

    public float bias;
    public float precision;
    public float recall;

    @JsonProperty("f_score")
    public float fScore;

    @JsonProperty("is_custom")
    public boolean isCustom;

    @JsonProperty("is_trained")
    public boolean isTrained;
}
