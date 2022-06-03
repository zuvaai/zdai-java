package ai.zuva.language;

import ai.zuva.exception.ZdaiApiException;
import ai.zuva.exception.ZdaiClientException;
import ai.zuva.http.Response;
import ai.zuva.http.ZdaiHttpClient;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.HashMap;

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
     * Given a ZdaiHttpClient and an array of fileIds, this constructor
     * makes a request to the Zuva servers to asynchronously classify
     * the language of each file. The created LanguageRequest
     * object can then be used to query the status and results of the
     * request for each individual file.
     *
     * @param  client  The client to use to make the request
     * @param  fileIds The IDs of the files to classify
     * @throws ZdaiClientException Error performing the requested action
     * @throws JsonProcessingException Unexpected JSON in request or response
     */
    public LanguageRequest(ZdaiHttpClient client, String fileId) throws ZdaiClientException, ZdaiApiException, JsonProcessingException {
        this.client = client;
        this.fileId = fileId;

        Response<String> response = client.authorizedRequest(
                "POST",
                "/language",
                client.mapper.writeValueAsString(new LanguageRequestBody(new String[]{fileId})),
                202
        );
        LanguageResults resp = client.mapper.readValue(response.getBody(), LanguageResults.class);
        this.requestId = resp.results[0].requestId;
    }

    /**
     * Construct a new object representing a pre-existing language request
     * <p>
     * Given a ZdaiHttpClient and a map of file IDs to request IDs, this constructor
     * makes a new LanguageRequest that can be used to obtain the status and
     * results of the given requests.
     *
     * @param  client  The client to use to make the request
     * @param  fileId  The file ID of the existing request
     * @param  requestId The request ID of the existing request
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
     * @throws ZdaiClientException Error performing the requested action
     * @throws JsonProcessingException Unexpected JSON in request or response
     */
    public LanguageResult getResult() throws ZdaiClientException, ZdaiApiException, JsonProcessingException {
        Response<String> response = client.authorizedRequest("GET", "/language/" + requestId, 200);
        return client.mapper.readValue(response.getBody(), LanguageResult.class);
    }
}
