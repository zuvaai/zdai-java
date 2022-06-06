package ai.zuva.fields;

import ai.zuva.exception.ZdaiApiException;
import ai.zuva.exception.ZdaiClientException;
import ai.zuva.http.ZdaiHttpClient;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Field {
    private ZdaiHttpClient client;
    public String fieldId;

    public Field(ZdaiHttpClient client, String fieldId) {
        this.client = client;
        this.fieldId = fieldId;
    }

    public static FieldListElement[] listFields(ZdaiHttpClient client) throws ZdaiClientException, ZdaiApiException {
        String response = client.authorizedRequest("GET", "/fields", 200);
        try {
            return client.mapper.readValue(response, FieldListElement[].class);
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to parse response", e));
        }
    }

    public FieldMetadata getMetadata() throws ZdaiClientException, ZdaiApiException {
        String response = client.authorizedRequest("GET", String.format("/fields/%s/metadata", fieldId), 200);
        try {
            return client.mapper.readValue(response, FieldMetadata.class);
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to parse response", e));
        }
    }

    // NameAndDescription is used to serialize an update metadata request
    static class NameAndDescription {
        public String name;
        public String description;

        public NameAndDescription(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    public void updateMetadata(String name, String description) throws ZdaiClientException, ZdaiApiException {
        String body;
        try {
            body = client.mapper.writeValueAsString(new NameAndDescription(name, description));
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to create request body", e));
        }

        client.authorizedRequest("PUT", String.format("/fields/%s/metadata", fieldId), body, 204);
    }

    public FieldAccuracy getAccuracy() throws ZdaiClientException, ZdaiApiException {
        String response = client.authorizedRequest("GET", String.format("/fields/%s/accuracy", fieldId), 200);

        try {
            return client.mapper.readValue(response, FieldAccuracy.class);
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to parse response", e));
        }
    }

    public FieldValidation[] getValidationDetails() throws ZdaiClientException, ZdaiApiException {
        String response = client.authorizedRequest("GET", String.format("/fields/%s/validation-details", fieldId), 200);

        try {
            return client.mapper.readValue(response, FieldValidation[].class);
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to parse response", e));
        }
    }

    public TrainingRequest createTrainingRequest(TrainingExample[] trainingExamples) throws ZdaiClientException, ZdaiApiException {
        return TrainingRequest.createTrainingRequest(client, fieldId, trainingExamples);
    }

    static class CreateFieldRequest {
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

    public static Field createField(ZdaiHttpClient client, String name, String description) throws ZdaiClientException, ZdaiApiException {
        String body;
        try {
            body = client.mapper.writeValueAsString(new CreateFieldRequest(name, description));
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to create request body", e));
        }

        String response = client.authorizedRequest("POST", "/fields", body, 201);

        try {
            return new Field(client, client.mapper.readValue(response, CreateFieldResponse.class).fieldId);
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to parse response", e));
        }
    }
}
