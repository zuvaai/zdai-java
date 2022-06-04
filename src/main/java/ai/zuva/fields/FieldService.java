package ai.zuva.fields;

import ai.zuva.exception.ZdaiApiException;
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

    public FieldListElement[] listFields() throws ZdaiClientException, ZdaiApiException {
        Response<String> response = client.authorizedRequest("GET", "/fields", 200);
        try {
            return client.mapper.readValue(response.getBody(), FieldListElement[].class);
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to parse response", e));
        }
    }

    public FieldMetadata getFieldMetadata(String fieldID) throws ZdaiClientException, ZdaiApiException {
        Response<String> response = client.authorizedRequest("GET", String.format("/fields/%s/metadata", fieldID), 200);
        try {
            return client.mapper.readValue(response.getBody(), FieldMetadata.class);
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to parse response", e));
        }
    }

    static class NameAndDescription {
        public String name;
        public String description;

        public NameAndDescription(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    public void updateFieldMetadata(String fieldID, String name, String desc) throws ZdaiClientException, ZdaiApiException {
        String body;
        try {
            body = client.mapper.writeValueAsString(new NameAndDescription(name, desc));
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to create request body", e));
        }

        client.authorizedRequest("PUT", String.format("/fields/%s/metadata", fieldID), body, 204);
    }

    public FieldAccuracy getFieldAccuracy(String fieldID) throws ZdaiClientException, ZdaiApiException {
        Response<String> response = client.authorizedRequest("GET", String.format("/fields/%s/accuracy", fieldID), 200);

        try {
            return client.mapper.readValue(response.getBody(), FieldAccuracy.class);
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to parse response", e));
        }
    }

    public FieldValidation[] getFieldValidationDetails(String fieldID) throws ZdaiClientException, ZdaiApiException {
        Response<String> response = client.authorizedRequest("GET", String.format("/fields/%s/validation-details", fieldID), 200);

        try {
            return client.mapper.readValue(response.getBody(), FieldValidation[].class);
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to parse response", e));
        }
    }

    class CreateFieldRequest {
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
    static class CreateFieldResponse {
        @JsonProperty("field_id")
        public String fieldId;
    }

    public String createField(String name, String description) throws ZdaiClientException, ZdaiApiException {
        String body;
        try {
            body = client.mapper.writeValueAsString(new CreateFieldRequest(name, description));
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to create request body", e));
        }

        Response<String> response = client.authorizedRequest("POST", "/fields", body, 201);

        try {
            return client.mapper.readValue(response.getBody(), CreateFieldResponse.class).fieldId;
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to parse response", e));
        }
    }
}
