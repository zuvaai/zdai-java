package ai.zuva.fields;

import ai.zuva.exception.ZdaiClientException;
import ai.zuva.http.Response;
import ai.zuva.http.ZdaiHttpClient;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldService {
    private ZdaiHttpClient client;

    public FieldService(ZdaiHttpClient client) {
        this.client = client;
    }

    public FieldListElement[] listFields() throws Exception {
        Response<String> response = client.authorizedRequest("GET", "/fields", 200);
        return client.mapper.readValue(response.getBody(), FieldListElement[].class);
    }

    public FieldMetadata getFieldMetadata(String fieldID) throws Exception {
        Response<String> response = client.authorizedRequest("GET", String.format("/fields/%s/metadata", fieldID), 200);
        return client.mapper.readValue(response.getBody(), FieldMetadata.class);
    }

    static class NameAndDescription {
        public String name;
        public String description;

        public NameAndDescription(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }
    public void updateFieldMetadata(String fieldID, String name, String desc) throws Exception {
        client.authorizedRequest("PUT",
                String.format("/fields/%s/metadata", fieldID),
                client.mapper.writeValueAsString(new NameAndDescription(name, desc)),
                204);
    }

    public FieldAccuracy getFieldAccuracy(String fieldID) throws Exception {
        Response<String> response = client.authorizedRequest("GET", String.format("/fields/%s/accuracy", fieldID), 200);
        return client.mapper.readValue(response.getBody(), FieldAccuracy.class);
    }

    public FieldValidation[] getFieldValidationDetails(String fieldID) throws Exception {
        Response<String> response = client.authorizedRequest("GET", String.format("/fields/%s/validation-details", fieldID), 200);
        return client.mapper.readValue(response.getBody(), FieldValidation[].class);

    }

    class CreateFieldRequest{
        @JsonProperty("field_name")
        public String name;

        @JsonProperty("description")
        public String description;
        public CreateFieldRequest(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class CreateFieldResponse{
        @JsonProperty("field_id")
        public String fieldId;
    }
    public String createField(String name, String description) throws Exception {
        Response<String> response = client.authorizedRequest(
                "POST",
                "/fields",
                client.mapper.writeValueAsString(new CreateFieldRequest(name, description)),
                201);

        return client.mapper.readValue(response.getBody(), CreateFieldResponse.class).fieldId;
    }
}
