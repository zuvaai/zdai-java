package ai.zuva.ocr;

import ai.zuva.exception.ZdaiClientException;
import ai.zuva.exception.ZdaiError;
import ai.zuva.http.Response;
import ai.zuva.http.ZdaiHttpClient;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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

    public OcrRequest(ZdaiHttpClient client, String fileId) throws Exception {
        this.client = client;
        this.fileId = fileId;

        Response<String> response = client.authorizedRequest("POST",
                "/ocr",
                client.mapper.writeValueAsString(new OcrRequestBody(new String[]{fileId})),
                202);

        OcrStatuses resp = client.mapper.readValue(response.getBody(), OcrStatuses.class);
        this.requestId = resp.statuses[0].requestId;
    }

    public OcrRequest(ZdaiHttpClient client, String fileId, String requestId) {
        this.client = client;
        this.fileId = fileId;
        this.requestId = requestId;
    }

    public String getStatus() throws Exception {
        Response<String> response = client.authorizedRequest("GET", "/ocr/" + requestId, 200);
        return client.mapper.readValue(response.getBody(), OcrStatus.class).status;
    }

    public String getText() throws Exception {
        Response<String> response = client.authorizedRequest("GET", "/ocr/" + requestId + "/text", 200);
        return client.mapper.readValue(response.getBody(), OcrText.class).text;
    }

    public byte[] getImages() throws Exception {
        Response<byte[]> response = client.authorizedRequest("/ocr/" + requestId + "/images", 200);
        return response.getBody();
    }

    public byte[] getLayouts() throws Exception {
        Response<byte[]> response = client.authorizedRequest("/ocr/" + requestId + "/layouts", 200);
        return response.getBody();
    }
}
