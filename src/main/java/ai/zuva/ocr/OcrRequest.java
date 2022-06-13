package ai.zuva.ocr;

import ai.zuva.BaseRequest;
import ai.zuva.api.ZdaiApiClient;
import ai.zuva.exception.ZdaiApiException;
import ai.zuva.exception.ZdaiClientException;
import ai.zuva.files.ZdaiFile;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OcrRequest extends BaseRequest {
    public final String fileId;

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
        @JsonProperty("file_ids")
        public String[] fileIds;

        public OcrRequestBody(ZdaiFile[] files) {
            this.fileIds = ZdaiFile.toFileIdArray(files);
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
    public static OcrRequest createRequest(ZdaiApiClient client, ZdaiFile file) throws ZdaiClientException, ZdaiApiException {
        return createRequests(client, new ZdaiFile[]{file})[0];
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
    public static OcrRequest[] createRequests(ZdaiApiClient client, ZdaiFile[] files) throws ZdaiClientException, ZdaiApiException {
        OcrStatuses resp = client.authorizedJsonRequest("POST", "/ocr", new OcrRequestBody(files), 202, OcrStatuses.class);
        OcrRequest[] ocrRequests = new OcrRequest[resp.statuses.length];
        for (int i = 0; i < ocrRequests.length; i++) {
            ocrRequests[i] = new OcrRequest(client, resp.statuses[i].fileId, resp.statuses[i].requestId);
        }
        return ocrRequests;
    }

    public OcrRequest(ZdaiApiClient client, String fileId, String requestId) {
        super(client, requestId);
        this.fileId = fileId;
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
    public OcrStatus getStatus() throws ZdaiClientException, ZdaiApiException {
        return client.authorizedGet("/ocr/" + requestId, 200, OcrStatus.class);
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
        return client.authorizedGet("/ocr/" + requestId + "/text", 200, OcrText.class).text;
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
