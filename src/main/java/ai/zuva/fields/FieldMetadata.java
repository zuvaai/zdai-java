package ai.zuva.fields;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldMetadata {
    @JsonProperty("field_id")
    public String fieldId;

    @JsonProperty("name")
    public String name;

    @JsonProperty("description")
    public String description;

    @JsonProperty("read_only")
    public boolean readOnly;

    @JsonProperty("is_trained")
    boolean isTrained;

    @JsonProperty("file_ids")
    public String[] fileIds;
}
