package ai.zuva.extraction;

import ai.zuva.exception.ZdaiApiException;
import ai.zuva.exception.ZdaiClientException;
import ai.zuva.exception.ZdaiError;
import ai.zuva.files.ZdaiFile;
import ai.zuva.http.ZdaiHttpClient;
import ai.zuva.ocr.OcrRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

public class ExtractionRequest {
    public final String fileId;
    public final String[] fieldIds;
    public final String requestId;
    public String status;
    public ZdaiError error;
    private final ZdaiHttpClient client;

    public ExtractionRequest(ZdaiHttpClient client, String requestId) {
        this.client = client;
        this.requestId = requestId;
        this.fileId = null;
        this.fieldIds = null;
    }

    private ExtractionRequest(ZdaiHttpClient client, ExtractionStatus extractionStatus) {
        this.client = client;
        this.fileId = extractionStatus.fileId;
        this.fieldIds = extractionStatus.fieldIds;
        this.requestId = extractionStatus.requestId;
        this.status = extractionStatus.status;
        this.error = extractionStatus.error;
    }

    static class ExtractionRequestBody {

        @JsonProperty("file_ids")
        public String[] fileIds;

        @JsonProperty("field_ids")
        public String[] fieldIds;

        public ExtractionRequestBody(String[] fileIds, String[] fieldIds) {
            this.fileIds = fileIds;
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
    static class ExtractionStatus {
        @JsonProperty("file_id")
        public String fileId;

        public String status;

        @JsonProperty("request_id")
        public String requestId;

        @JsonProperty("field_ids")
        public String[] fieldIds;

        // Expect this to be populated only when status is failed
        @JsonProperty("error")
        public ZdaiError error;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ExtractionStatuses {
        @JsonProperty("file_ids")
        public ExtractionStatus[] statuses;
    }

    /**
     * Construct and send a request to extract fields from a file
     * <p>
     * Given a ZdaiHttpClient, a fileId, and an array of field IDs, this
     * constructor makes a request to the Zuva servers to asynchronously extract
     * the specified fields from the file
     *
     * @param client   The client to use to make the request
     * @param file   The file to analyze
     * @param fieldIds The IDs of the fields to extract from the file
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public static ExtractionRequest createExtractionRequest(ZdaiHttpClient client, ZdaiFile file, String[] fieldIds) throws ZdaiClientException, ZdaiApiException {
        return createExtractionRequests(client, new ZdaiFile[]{file}, fieldIds)[0];
    }

    /**
     * Construct and send a request to extract fields from multiple files
     * <p>
     * Given a ZdaiHttpClient, a fileId, and an array of field IDs, this
     * constructor makes a request to the Zuva servers to asynchronously extract
     * the specified fields from the file
     *
     * @param client   The client to use to make the request
     * @param files   The files to analyze
     * @param fieldIds The IDs of the fields to extract from the file
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public static ExtractionRequest[] createExtractionRequests(ZdaiHttpClient client, ZdaiFile[] files, String[] fieldIds) throws ZdaiClientException, ZdaiApiException {
        String[] fileIds = new String[files.length];

        for (int i = 0; i < files.length; i++) {
            fileIds[i] = files[i].fileId;
        }

        String body;
        try {
            body = client.mapper.writeValueAsString(new ExtractionRequestBody(fileIds, fieldIds));
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to create request body", e));
        }
        String response = client.authorizedRequest("POST", "/extraction", body, 202);

        try {
            ExtractionStatuses resp = client.mapper.readValue(response, ExtractionStatuses.class);
            ExtractionRequest[] extractionRequests = new ExtractionRequest[resp.statuses.length];
            for (int i = 0; i < extractionRequests.length; i++) {
                extractionRequests[i] = new ExtractionRequest(client, resp.statuses[i]);
            }
            return extractionRequests;
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to parse response", e));
        }
    }

    /**
     * Get status of extraction request from the Zuva server
     * <p>
     * Given a ZdaiHttpClient, return a String indicating the status of the
     * request.
     *
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public String getStatus() throws ZdaiClientException, ZdaiApiException {
        String response = client.authorizedRequest("GET", String.format("/extraction/%s", requestId), 200);

        try {
            status = client.mapper.readValue(response, ExtractionStatus.class).status;
            return status;
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to parse response", e));
        }
    }

    /**
     * Get results of a successful extraction request from the Zuva server
     * <p>
     * Given a ZdaiHttpClient, return an array of ExtractionResults containing
     * the text and location of all extractions for each field.
     *
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public ExtractionResults[] getResults() throws ZdaiClientException, ZdaiApiException {
        String response = client.authorizedRequest("GET", "/extraction/" + requestId + "/results/text", 200);
        try {
            return client.mapper.readValue(response, ExtractionResultsBody.class).results;
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to parse response", e));
        }
    }
}
