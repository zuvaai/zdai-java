package ai.zuva.classification;

import ai.zuva.exception.ZdaiApiException;
import ai.zuva.exception.ZdaiClientException;
import ai.zuva.files.ZdaiFile;
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
     * Send a request to classify the document type of a file.
     * <p>
     * Given a ZdaiHttpClient and a ZdaiFile, make a request to the Zuva servers to
     * asynchronously classify the document type of the file. The created ClassificationRequest
     * object can then be used to query the status and results of the request.
     *
     * @param client The client to use to make the request
     * @param file   The file to classify
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public static ClassificationRequest createClassificationRequest(ZdaiHttpClient client, ZdaiFile file) throws ZdaiClientException, ZdaiApiException {
        return createClassificationRequests(client, new ZdaiFile[]{file})[0];
    }

    /**
     * Send a request to classify the document type of multiple files.
     * <p>
     * Given a ZdaiHttpClient and an array of ZdaiFiles, make a request to the Zuva servers to
     * asynchronously classify the document type of the files. The created ClassificationRequests
     * objects can then be used to query the status and results of each request.
     *
     * @param client The client to use to make the request
     * @param files  The files to classify
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public static ClassificationRequest[] createClassificationRequests(ZdaiHttpClient client, ZdaiFile[] files) throws ZdaiClientException, ZdaiApiException {
        String[] fileIds = new String[files.length];

        for (int i = 0; i < files.length; i++) {
            fileIds[i] = files[i].fileId;
        }

        String body;
        try {
            body = client.mapper.writeValueAsString(new ClassificationRequestBody(fileIds));
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Error creating request body", e));
        }

        String response = client.authorizedRequest("POST", "/classification", body, 202);

        try {
            ClassificationResultsBody resp = client.mapper.readValue(response, ClassificationResultsBody.class);
            ClassificationRequest[] classificationRequests = new ClassificationRequest[resp.results.length];
            for (int i = 0; i < classificationRequests.length; i++) {
                classificationRequests[i] = new ClassificationRequest(client, files[i].fileId, resp.results[i].requestId);
            }
            return classificationRequests;
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to parse response", e));
        }
    }

    /**
     * Construct a new object representing a pre-existing classification request
     * <p>
     * Given a ZdaiHttpClient and a map of file IDs to request IDs, this constructor
     * makes a new ClassificationRequest that can be used to obtain the status and
     * results of the given request.
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
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public ClassificationResult getClassificationResult() throws ZdaiClientException, ZdaiApiException {
        String response = client.authorizedRequest("GET", "/classification/" + requestId, 200);
        try {
            return client.mapper.readValue(response, ClassificationResult.class);
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to parse response", e));
        }
    }
}
