package ai.zuva.classification;

import ai.zuva.exception.ZdaiApiException;
import ai.zuva.exception.ZdaiClientException;
import ai.zuva.http.Response;
import ai.zuva.http.ZdaiHttpClient;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

public class ClassificationRequest {
    public final String fileId;
    public final String requestId;
    private final ZdaiHttpClient client;

    // This class is used internally to construct the JSON body for the POST /classification request
    static class ClassificationRequestBody {
        @JsonProperty("file_ids")
        public String[] fileIds;

        public ClassificationRequestBody(String[] fileIds) {
            this.fileIds = fileIds;
        }
    }

    // This class is used internally to read the JSON body from the POST /classification request
    static class ClassificationResultsBody {
        @JsonProperty("file_ids")
        public ClassificationResult[] results;
    }

    /**
     * Construct and send a request to classify the document type.
     * <p>
     * Given a ZdaiHttpClient and an array of fileIds, this constructor
     * makes a request to the Zuva servers to asynchronously classify
     * the document type of each file. The created ClassificationRequest
     * object can then be used to query the status and results of the
     * request for each individual file.
     *
     * @param client The client to use to make the request
     * @param fileId The ID of the file to classify
     * @throws ZdaiClientException     Error performing the requested action
     * @throws JsonProcessingException Unexpected JSON in request or response
     */
    public ClassificationRequest(ZdaiHttpClient client, String fileId) throws ZdaiClientException, ZdaiApiException, JsonProcessingException {
        this.client = client;
        this.fileId = fileId;

        Response<String> response = client.authorizedRequest(
                "POST",
                "/classification",
                client.mapper.writeValueAsString(new ClassificationRequestBody(new String[]{fileId})),
                202);


        ClassificationResultsBody resp = client.mapper.readValue(response.getBody(), ClassificationResultsBody.class);
        this.requestId = resp.results[0].requestId;
    }

    /**
     * Construct a new object representing a pre-existing classification request
     * <p>
     * Given a ZdaiHttpClient and a map of file IDs to request IDs, this constructor
     * makes a new ClassificationRequest that can be used to obtain the status and
     * results of the given requests.
     *
     * @param client    The client to use to make the request
     * @param fileId    The ID of the file being classified
     * @param requestId The ID of an existing request.
     */
    public ClassificationRequest(ZdaiHttpClient client, String fileId, String requestId) {
        this.client = client;
        this.fileId = fileId;
        this.requestId = requestId;
    }

    /**
     * Get classification status and results from the Zuva server
     * <p>
     * Given a ZdaiHttpClient, return a ClassificationResult indicating the
     * status of the classification request for that file and the classification
     * result (if available).
     *
     * @throws ZdaiClientException     Error performing the requested action
     * @throws JsonProcessingException Unexpected JSON in request or response
     */
    public ClassificationResult getClassificationResult() throws ZdaiClientException, ZdaiApiException, JsonProcessingException {
        Response<String> response = client.authorizedRequest("GET", "/classification/" + requestId, 200);
        return client.mapper.readValue(response.getBody(), ClassificationResult.class);
    }
}
