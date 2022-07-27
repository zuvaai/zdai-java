package ai.zuva.fields;

import ai.zuva.api.DocAIClient;
import ai.zuva.exception.DocAIApiException;
import ai.zuva.exception.DocAIClientException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Field {
    private final DocAIClient client;
    public String fieldId;

    /**
     * @param client  The client to use when interacting with this field
     * @param fieldId The ID of the field
     */
    public Field(DocAIClient client, String fieldId) {
        this.client = client;
        this.fieldId = fieldId;
    }

    /**
     * Queries the Zuva DocAI API for a list of all available fields
     *
     * @param client The client to use to make the request
     * @return A list of all the fields available to the client
     * @throws DocAIApiException    Unsuccessful response code from server
     * @throws DocAIClientException Error preparing, sending or processing the request/response
     */
    public static FieldListElement[] listFields(DocAIClient client) throws DocAIClientException, DocAIApiException {
        return client.authorizedGet("api/v2/fields", 200, FieldListElement[].class);
    }

    /**
     * Queries the Zuva DocAI API for metadata about the field
     *
     * @return A FieldMetadata object containing metadata about this field
     * @throws DocAIApiException    Unsuccessful response code from server
     * @throws DocAIClientException Error preparing, sending or processing the request/response
     */
    public FieldMetadata getMetadata() throws DocAIClientException, DocAIApiException {
        return client.authorizedGet(String.format("api/v2/fields/%s/metadata", fieldId), 200, FieldMetadata.class);
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

    /**
     * Updates the name and description of a custom field
     *
     * @param name        The new name for the field
     * @param description The new descriptionf of the field
     * @throws DocAIApiException    Unsuccessful response code from server
     * @throws DocAIClientException Error preparing, sending or processing the request/response
     */
    public void updateMetadata(String name, String description) throws DocAIClientException, DocAIApiException {
        client.authorizedJsonRequest("PUT",
                String.format("api/v2/fields/%s/metadata", fieldId),
                new NameAndDescription(name, description),
                204,
                null);
    }

    /**
     * Queries the Zuva DocAI API for the field's accuracy scores
     *
     * @return A FieldAccuracy object that includes the precision, recall and f-score of the field
     * @throws DocAIApiException    Unsuccessful response code from server
     * @throws DocAIClientException Error preparing, sending or processing the request/response
     */
    public FieldAccuracy getAccuracy() throws DocAIClientException, DocAIApiException {
        return client.authorizedGet(String.format("api/v2/fields/%s/accuracy", fieldId), 200, FieldAccuracy.class);
    }

    /**
     * Queries the Zuva DocAI API for the field's validation details
     *
     * @return An array of FieldValidation objects
     * @throws DocAIApiException    Unsuccessful response code from server
     * @throws DocAIClientException Error preparing, sending or processing the request/response
     */
    public FieldValidation[] getValidationDetails() throws DocAIClientException, DocAIApiException {
        return client.authorizedGet(String.format("api/v2/fields/%s/validation-details", fieldId), 200, FieldValidation[].class);
    }

    /**
     * Send a request to train this field from examples
     * <p>
     * Given a ZdaiApiClient, a fileId, a field ID, and training examples make a request
     * to the Zuva servers to asynchronously train a new version of the field on the specified
     * data. The returned TrainingRequest object can then be used to check the status of the
     * training process.
     *
     * @param trainingExamples An array of Training examples describing the character spans that this
     *                         field should extract from each training file
     * @return A TrainingRequest object, which can be used to check the status of the training request
     * @throws DocAIApiException    Unsuccessful response code from server
     * @throws DocAIClientException Error preparing, sending or processing the request/response
     */
    public TrainingRequest createTrainingRequest(TrainingExample[] trainingExamples) throws DocAIClientException, DocAIApiException {
        return TrainingRequest.createRequest(client, fieldId, trainingExamples);
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

    /**
     * @param client      The client to use to make the request
     * @param name        The name of the field
     * @param description The description of the field
     * @return A Field object representing the new untrained field
     * @throws DocAIApiException    Unsuccessful response code from server
     * @throws DocAIClientException Error preparing, sending or processing the request/response
     */
    public static Field createField(DocAIClient client, String name, String description) throws DocAIClientException, DocAIApiException {
        CreateFieldResponse response = client.authorizedJsonRequest(
                "POST",
                "api/v2/fields",
                new CreateFieldRequest(name, description),
                201,
                CreateFieldResponse.class);
        return new Field(client, response.fieldId);
    }
}
