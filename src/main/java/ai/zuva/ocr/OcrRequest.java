package ai.zuva.ocr;

import ai.zuva.exception.ZdaiApiException;
import ai.zuva.exception.ZdaiClientException;
import ai.zuva.exception.ZdaiError;
import ai.zuva.http.ZdaiHttpClient;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

public class OcrRequest {
    public final String requestId;
    public final String fileId;

    private final ZdaiHttpClient client;

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
     * send a request to extract fields from a file
     * <p>
     * Given a ZdaiHttpClient, a fileId, this constructor makes a request to
     * the Zuva servers to asynchronously perform OCR on the file, returning an
     * OcrRequest object that can subsequently be used to obtain the file's
     * text, images and layouts.
     *
     * @param client   The client to use to make the request
     * @param fileId   The ID of the file to analyze
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public static OcrRequest createOcrRequest(ZdaiHttpClient client, String fileId) throws ZdaiClientException, ZdaiApiException {
        String body;
        try {
            body = client.mapper.writeValueAsString(new OcrRequestBody(new String[]{fileId}));
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to create request body", e));
        }

        String response = client.authorizedRequest("POST", "/ocr", body, 202);

        try {
            OcrStatuses resp = client.mapper.readValue(response, OcrStatuses.class);
            return new OcrRequest(client, fileId, resp.statuses[0].requestId);
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to parse response", e));
        }
    }

    public OcrRequest(ZdaiHttpClient client, String fileId, String requestId) {
        this.client = client;
        this.fileId = fileId;
        this.requestId = requestId;
    }

    /**
     * Get status of an OCR request from the Zuva server
     * <p>
     * Given a ZdaiHttpClient, return a String indicating the status of the
     * request.
     *
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public String getStatus() throws ZdaiClientException, ZdaiApiException {
        String response = client.authorizedRequest("GET", "/ocr/" + requestId, 200);
        try {
            return client.mapper.readValue(response, OcrStatus.class).status;
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to parse response", e));
        }
    }

    /**
     * Get text results of an OCR request from the Zuva server
     * <p>
     * Given a ZdaiHttpClient, return the OCR text of the document as
     * a String.
     *
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public String getText() throws ZdaiClientException, ZdaiApiException {
        String response = client.authorizedRequest("GET", "/ocr/" + requestId + "/text", 200);
        try {
            return client.mapper.readValue(response, OcrText.class).text;
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to parse response", e));
        }
    }

    /**
     * Get image results of an OCR request from the Zuva server
     * <p>
     * Given a ZdaiHttpClient, return the OCR images of the document as
     * a zip file containing a PNG image of each page.
     *
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public byte[] getImages() throws ZdaiClientException, ZdaiApiException {
        return client.authorizedRequest("/ocr/" + requestId + "/images", 200);
    }

    /**
     * Get layout results of an OCR request from the Zuva server
     * <p>
     * Given a ZdaiHttpClient, return the layout of the document in a
     * protobuff format
     *
     * @throws ZdaiApiException    Unsuccessful response code from server
     * @throws ZdaiClientException Error preparing, sending or processing the request/response
     */
    public byte[] getLayouts() throws ZdaiClientException, ZdaiApiException {
        return client.authorizedRequest("/ocr/" + requestId + "/layouts", 200);
    }
}
