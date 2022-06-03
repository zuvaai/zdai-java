package ai.zuva.files;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmitFileResponse {
    @JsonProperty("file_id")
    public String fileId;
    public FileAttributes attributes;
    public String[] permissions;
    public String expiration;
}
