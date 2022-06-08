package ai.zuva.ocr;

import ai.zuva.exception.ZdaiApiException;
import ai.zuva.exception.ZdaiClientException;
import ai.zuva.exception.ZdaiError;
import ai.zuva.files.ZdaiFile;
import ai.zuva.api.ZdaiApiClient;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

public class OcrRequest {
    public final String requestId;
    public final String fileId;

    private final ZdaiApiClient client;

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class OcrStatus {
        @JsonProperty("file_id")
        public String fileId;

        public String status;
        @JsonProperty("request_id")
        public String requestId;

        // Expect this to be populated only when status is failed
        @JsonProperty("error")
        public ZdaiError error;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class OcrStatuses {
        @JsonProperty("file_ids")
        public OcrStatus[] statuses;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class OcrText {
        @JsonProperty("request_id")
        public String requestId;
        public String text;
    }

    static class OcrRequestBody {
        public String[] file_ids;

        public OcrRequestBody(String[] fileIds) {
            this.file_ids = fileIds;
        }
    }

    /**
     * Send a request to perform OCR on a file
     * <p>
     * Given a ZdaiApiClient, a fileId, this constructor makes a request to
     * the Zuva servers to asynchronously perform OCR on the file, returning an
     * OcrRequest object that can subsequently be used to obtain the file's
     * text, images and layouts.
     *
     * @param client The client to use to make the request
     * @param file   The file to analyze
     * @return An OcrRequest object, which can be used to check the status and results of the request
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public static OcrRequest createOcrRequest(ZdaiApiClient client, ZdaiFile file) throws ZdaiClientException, ZdaiApiException {
        return createOcrRequests(client, new ZdaiFile[]{file})[0];
    }

    /**
     * Send a request to extract fields from multiple files
     * <p>
     * Given a ZdaiApiClient, a fileId, this constructor makes a request to
     * the Zuva servers to asynchronously perform OCR on the file, returning an
     * OcrRequest object that can subsequently be used to obtain the file's
     * text, images and layouts.
     *
     * @param client The client to use to make the request
     * @param files  The files to analyze
     * @return An array of OcrRequest objects, which can be used to check the status and results of the requests
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public static OcrRequest[] createOcrRequests(ZdaiApiClient client, ZdaiFile[] files) throws ZdaiClientException, ZdaiApiException {
        String[] fileIds = new String[files.length];

        for (int i = 0; i < files.length; i++) {
            fileIds[i] = files[i].fileId;
        }

        String body;
        try {
            body = client.mapper.writeValueAsString(new OcrRequestBody(fileIds));
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to create request body", e));
        }

        String response = client.authorizedRequest("POST", "/ocr", body, 202);

        try {
            OcrStatuses resp = client.mapper.readValue(response, OcrStatuses.class);
            OcrRequest[] ocrRequests = new OcrRequest[resp.statuses.length];
            for (int i = 0; i < ocrRequests.length; i++) {
                ocrRequests[i] = new OcrRequest(client, files[i].fileId, resp.statuses[i].requestId);
            }
            return ocrRequests;
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to parse response", e));
        }
    }

    public OcrRequest(ZdaiApiClient client, String fileId, String requestId) {
        this.client = client;
        this.fileId = fileId;
        this.requestId = requestId;
    }

    /**
     * Get status of an OCR request from the Zuva server
     * <p>
     * Given a ZdaiApiClient, return a String indicating the status of the
     * request.
     *
     * @return The request status as a String (one of "queued", "processing", "complete" or "failed")
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public String getStatus() throws ZdaiClientException, ZdaiApiException {
        String response = client.authorizedGet("/ocr/" + requestId, 200);
        try {
            return client.mapper.readValue(response, OcrStatus.class).status;
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to parse response", e));
        }
    }

    /**
     * Get text results of an OCR request from the Zuva server
     * <p>
     * Given a ZdaiApiClient, return the OCR text of the document as
     * a String.
     *
     * @return The text of the document as a String
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public String getText() throws ZdaiClientException, ZdaiApiException {
        String response = client.authorizedGet("/ocr/" + requestId + "/text", 200);
        try {
            return client.mapper.readValue(response, OcrText.class).text;
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to parse response", e));
        }
    }

    /**
     * Get image results of an OCR request from the Zuva server
     *
     * @return A byte array of the OCR images of the document as a zip file containing a PNG image of each page.
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public byte[] getImages() throws ZdaiClientException, ZdaiApiException {
        return client.authorizedGetBinary("/ocr/" + requestId + "/images", 200);
    }

    /**
     * Get layout results of an OCR request from the Zuva server
     *
     * @return The layout of the document in a protobuff format, as a byte array
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public byte[] getLayouts() throws ZdaiClientException, ZdaiApiException {
        return client.authorizedGetBinary("/ocr/" + requestId + "/layouts", 200);
    }
}
