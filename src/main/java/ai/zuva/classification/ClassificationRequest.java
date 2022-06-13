package ai.zuva.classification;

import ai.zuva.BaseRequest;
import ai.zuva.api.ZdaiApiClient;
import ai.zuva.exception.ZdaiApiException;
import ai.zuva.exception.ZdaiClientException;
import ai.zuva.files.ZdaiFile;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ClassificationRequest extends BaseRequest {
    public final String fileId;

    // This class is used internally to construct the JSON body for the POST /classification request
    static class ClassificationRequestBody {
        @JsonProperty("file_ids")
        public String[] fileIds;

        public ClassificationRequestBody(ZdaiFile[] files) {
            this.fileIds = ZdaiFile.toFileIdArray(files);
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
     * Given a ZdaiApiClient and a ZdaiFile, make a request to the Zuva servers to
     * asynchronously classify the document type of the file. The created ClassificationRequest
     * object can then be used to query the status and results of the request.
     *
     * @param client The client to use to make the request
     * @param file   The file to classify
     * @return A ClassificationRequest, which can be used to check the status and results of the request
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public static ClassificationRequest createRequest(ZdaiApiClient client, ZdaiFile file) throws ZdaiClientException, ZdaiApiException {
        return createRequests(client, new ZdaiFile[]{file})[0];
    }

    /**
     * Send a request to classify the document type of multiple files.
     * <p>
     * Given a ZdaiApiClient and an array of ZdaiFiles, make a request to the Zuva servers to
     * asynchronously classify the document type of the files. The created ClassificationRequests
     * objects can then be used to query the status and results of each request.
     *
     * @param client The client to use to make the request
     * @param files  The files to classify
     * @return An array of ClassificationRequests, which can be used to check the status and results of the requests
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public static ClassificationRequest[] createRequests(ZdaiApiClient client, ZdaiFile[] files) throws ZdaiClientException, ZdaiApiException {
        ClassificationResultsBody resp = client.authorizedJsonRequest(
                "POST",
                "api/v2/classification",
                new ClassificationRequestBody(files),
                202,
                ClassificationResultsBody.class);

        ClassificationRequest[] classificationRequests = new ClassificationRequest[resp.results.length];
        for (int i = 0; i < classificationRequests.length; i++) {
            classificationRequests[i] = new ClassificationRequest(client, resp.results[i]);
        }
        return classificationRequests;
    }

    private ClassificationRequest(ZdaiApiClient client, ClassificationResult result) {
        super(client, result);
        this.fileId = result.fileId;
    }

    /**
     * Construct a new object representing a pre-existing classification request
     * <p>
     * Given a ZdaiApiClient and a map of file IDs to request IDs, this constructor
     * makes a new ClassificationRequest that can be used to obtain the status and
     * results of the given request.
     *
     * @param client    The client to use to make the request
     * @param fileId    The ID of the file being classified
     * @param requestId The ID of an existing request.
     */
    public ClassificationRequest(ZdaiApiClient client, String fileId, String requestId) {
        super(client, requestId, null, null);
        this.fileId = fileId;
    }

    /**
     * Get classification status and results from the Zuva server
     * <p>
     * Given a ZdaiApiClient, return a ClassificationResult indicating the
     * status of the classification request for that file and the classification
     * result (if available).
     *
     * @return A ClassificationResult, with the status and results of the request
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public ClassificationResult getStatus() throws ZdaiClientException, ZdaiApiException {
        return client.authorizedGet("api/v2/classification/" + requestId, 200, ClassificationResult.class);
    }
}
