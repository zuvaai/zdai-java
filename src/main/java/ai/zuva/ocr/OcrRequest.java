package ai.zuva.ocr;

import ai.zuva.BaseRequest;
import ai.zuva.api.DocAIClient;
import ai.zuva.exception.DocAIApiException;
import ai.zuva.exception.DocAIClientException;
import ai.zuva.files.File;
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

        public OcrRequestBody(File[] files) {
            this.fileIds = File.toFileIdArray(files);
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
     * @throws DocAIApiException    Unsuccessful response code from server
     * @throws DocAIClientException Error preparing, sending or processing the request/response
     */
    public static OcrRequest createRequest(DocAIClient client, File file) throws DocAIClientException, DocAIApiException {
        return createRequests(client, new File[]{file})[0];
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
     * @throws DocAIApiException    Unsuccessful response code from server
     * @throws DocAIClientException Error preparing, sending or processing the request/response
     */
    public static OcrRequest[] createRequests(DocAIClient client, File[] files) throws DocAIClientException, DocAIApiException {
        OcrStatuses resp = client.authorizedJsonRequest("POST", "api/v2/ocr", new OcrRequestBody(files), 202, OcrStatuses.class);
        OcrRequest[] ocrRequests = new OcrRequest[resp.statuses.length];
        for (int i = 0; i < ocrRequests.length; i++) {
            ocrRequests[i] = new OcrRequest(client, resp.statuses[i]);
        }
        return ocrRequests;
    }

    private OcrRequest(DocAIClient client, OcrStatus status) {
        super(client, status);
        this.fileId = status.fileId;
    }

    public OcrRequest(DocAIClient client, String requestId) {
        super(client, requestId, null, null);
        this.fileId = null;
    }

    /**
     * Get status of an OCR request from the Zuva server
     * <p>
     * Given a ZdaiApiClient, return a String indicating the status of the
     * request.
     *
     * @return The request status as a String (one of "queued", "processing", "complete" or "failed")
     * @throws DocAIApiException    Unsuccessful response code from server
     * @throws DocAIClientException Error preparing, sending or processing the request/response
     */
    public OcrStatus getStatus() throws DocAIClientException, DocAIApiException {
        return client.authorizedGet("api/v2/ocr/" + requestId, 200, OcrStatus.class);
    }

    /**
     * Get text results of an OCR request from the Zuva server
     * <p>
     * Given a ZdaiApiClient, return the OCR text of the document as
     * a String.
     *
     * @return The text of the document as a String
     * @throws DocAIApiException    Unsuccessful response code from server
     * @throws DocAIClientException Error preparing, sending or processing the request/response
     */
    public String getText() throws DocAIClientException, DocAIApiException {
        return client.authorizedGet("api/v2/ocr/" + requestId + "/text", 200, OcrText.class).text;
    }

    /**
     * Get image results of an OCR request from the Zuva server
     *
     * @return A byte array of the OCR images of the document as a zip file containing a PNG image of each page.
     * @throws DocAIApiException    Unsuccessful response code from server
     * @throws DocAIClientException Error preparing, sending or processing the request/response
     */
    public byte[] getImages() throws DocAIClientException, DocAIApiException {
        return client.authorizedGetBinary("api/v2/ocr/" + requestId + "/images", 200);
    }

    /**
     * Get layout results of an OCR request from the Zuva server
     *
     * @return The layout of the document in a protobuff format, as a byte array
     * @throws DocAIApiException    Unsuccessful response code from server
     * @throws DocAIClientException Error preparing, sending or processing the request/response
     */
    public byte[] getLayouts() throws DocAIClientException, DocAIApiException {
        return client.authorizedGetBinary("api/v2/ocr/" + requestId + "/layouts", 200);
    }
}
