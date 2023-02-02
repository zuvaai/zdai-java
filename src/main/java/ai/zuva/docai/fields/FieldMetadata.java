package ai.zuva.docai.fields;

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

  @JsonProperty("document_types")
  public DocumentType[] documentType;

  @JsonProperty("languages")
  public Languages[] languages;

  @JsonProperty("jurisdictions")
  public Jurisdictions[] jurisdictions;

  @JsonProperty("tags")
  public String[] tags;

  @JsonProperty("normalization_type")
  public String normalizationType;
}
