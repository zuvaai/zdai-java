package ai.zuva.language;

import ai.zuva.exception.ZdaiApiException;
import ai.zuva.exception.ZdaiClientException;
import ai.zuva.http.ZdaiHttpClient;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

public class LanguageRequest {
    /**
     * A map of file IDs to their corresponding classification request IDs.
     */
    public final String fileId;
    public final String requestId;

    private final ZdaiHttpClient client;

    // This class is used internally to construct the JSON body for the POST /language request
    static class LanguageRequestBody {
        @JsonProperty("file_ids")
        public String[] fileIds;

        public LanguageRequestBody(String[] fileIds) {
            this.fileIds = fileIds;
        }
    }

    // This class is used internally to read the JSON body from the POST /language request
    static class LanguageResults {
        @JsonProperty("file_ids")
        public LanguageResult[] results;
    }

    /**
     * Construct and send a request to classify the document's primary language.
     * <p>
     * Given a ZdaiHttpClient and a fileId, this constructor makes a request to the
     * Zuva API to asynchronously classify the language of the file. The created LanguageRequest
     * object can then be used to query the status and results of the request.
     *
     * @param client  The client to use to make the request
     * @param fileId The ID of the file to classify
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public LanguageRequest(ZdaiHttpClient client, String fileId) throws ZdaiClientException, ZdaiApiException {
        this.client = client;
        this.fileId = fileId;

        String body;

        try {
            body = client.mapper.writeValueAsString(new LanguageRequestBody(new String[]{fileId}));
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Error creating request body", e));
        }

        String response = client.authorizedRequest("POST", "/language", body, 202);

        try {
            LanguageResults resp = client.mapper.readValue(response, LanguageResults.class);
            this.requestId = resp.results[0].requestId;
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to parse response", e));
        }
    }

    /**
     * Construct a new object representing a pre-existing language request
     * <p>
     * Given a ZdaiHttpClient, a file ID and a request ID, construct a new
     * LanguageRequest object that can be used to obtain the status and results
     * of the given requests.
     *
     * @param client    The client to use to make the request
     * @param fileId    The file ID of the existing request
     * @param requestId The request ID of the existing request
     */
    public LanguageRequest(ZdaiHttpClient client, String fileId, String requestId) {
        this.client = client;
        this.fileId = fileId;
        this.requestId = requestId;
    }

    /**
     * Get language status and results from the Zuva server
     * <p>
     * Given a ZdaiHttpClient, return a LanguageResult indicating the status of the
     * request and the language result (if available).
     *
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public LanguageResult getResult() throws ZdaiClientException, ZdaiApiException {
        String response = client.authorizedRequest("GET", "/language/" + requestId, 200);
        try {
            return client.mapper.readValue(response, LanguageResult.class);
        } catch (JsonProcessingException e){
            throw(new ZdaiClientException("Unable to parse response", e));
        }
    }
}
