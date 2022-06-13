package ai.zuva.extraction;

import ai.zuva.BaseRequest;
import ai.zuva.ProcessingState;
import ai.zuva.api.ZdaiApiClient;
import ai.zuva.exception.ZdaiApiException;
import ai.zuva.exception.ZdaiClientException;
import ai.zuva.exception.ZdaiError;
import ai.zuva.files.ZdaiFile;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ExtractionRequest extends BaseRequest {
    public final String fileId;
    public final String[] fieldIds;

    static class ExtractionRequestBody {

        @JsonProperty("file_ids")
        public String[] fileIds;

        @JsonProperty("field_ids")
        public String[] fieldIds;

        public ExtractionRequestBody(ZdaiFile[] files, String[] fieldIds) {
            this.fileIds = ZdaiFile.toFileIdArray(files);
            this.fieldIds = fieldIds;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ExtractionResultsBody {
        @JsonProperty("file_id")
        public String fileId;

        @JsonProperty("request_id")
        public String requestId;

        public ExtractionResults[] results;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ExtractionStatuses {
        @JsonProperty("file_ids")
        public ExtractionStatus[] statuses;
    }

    /**
     * Construct and send a request to extract fields from a file
     * <p>
     * Given a ZdaiApiClient, a fileId, and an array of field IDs, this
     * constructor makes a request to the Zuva servers to asynchronously extract
     * the specified fields from the file
     *
     * @param client   The client to use to make the request
     * @param file     The file to analyze
     * @param fieldIds The IDs of the fields to extract from the file
     * @return An ExtractionRequest object, which can be used to check the status and results of the request
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public static ExtractionRequest createRequest(ZdaiApiClient client, ZdaiFile file, String[] fieldIds) throws ZdaiClientException, ZdaiApiException {
        return createRequests(client, new ZdaiFile[]{file}, fieldIds)[0];
    }

    /**
     * Construct and send a request to extract fields from multiple files
     * <p>
     * Given a ZdaiApiClient, a fileId, and an array of field IDs, this
     * constructor makes a request to the Zuva servers to asynchronously extract
     * the specified fields from the file
     *
     * @param client   The client to use to make the request
     * @param files    The files to analyze
     * @param fieldIds The IDs of the fields to extract from the file
     * @return An array of ExtractionRequest objects, which can be used to check the status and results of the requests
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public static ExtractionRequest[] createRequests(ZdaiApiClient client, ZdaiFile[] files, String[] fieldIds) throws ZdaiClientException, ZdaiApiException {
        ExtractionStatuses resp = client.authorizedJsonRequest(
                "POST",
                "/extraction",
                new ExtractionRequestBody(files, fieldIds),
                202,
                ExtractionStatuses.class);

        ExtractionRequest[] extractionRequests = new ExtractionRequest[resp.statuses.length];
        for (int i = 0; i < extractionRequests.length; i++) {
            extractionRequests[i] = new ExtractionRequest(client, resp.statuses[i]);
        }
        return extractionRequests;
    }

    // Constructor is private since it is only used by the above static factory methods
    private ExtractionRequest(ZdaiApiClient client, ExtractionStatus extractionStatus) {
        super(client, extractionStatus);
        this.fileId = extractionStatus.fileId;
        this.fieldIds = extractionStatus.fieldIds;
    }

    public ExtractionRequest(ZdaiApiClient client, String requestId) {
        super(client, requestId, null, null);
        this.fileId = null;
        this.fieldIds = null;
    }

    /**
     * Get status of extraction request from the Zuva server
     * <p>
     * Given a ZdaiApiClient, return a String indicating the status of the
     * request.
     *
     * @return The request status as a String (one of "queued", "processing", "complete" or "failed")
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public ExtractionStatus getStatus() throws ZdaiClientException, ZdaiApiException {
        return client.authorizedGet(String.format("/extraction/%s", requestId), 200, ExtractionStatus.class);
    }

    /**
     * Get results of a successful extraction request from the Zuva server
     * <p>
     * Given a ZdaiApiClient, return an array of ExtractionResults containing
     * the text and location of all extractions for each field.
     *
     * @return An array of ExtractionResult objects, containing the results of the extraction
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public ExtractionResults[] getResults() throws ZdaiClientException, ZdaiApiException {
        return client.authorizedGet("/extraction/" + requestId + "/results/text", 200, ExtractionResultsBody.class).results;
    }
}
