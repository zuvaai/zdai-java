package ai.zuva.extraction;

import ai.zuva.exception.ZdaiError;
import ai.zuva.http.Response;
import ai.zuva.http.ZdaiHttpClient;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ExtractionRequest {
    public final String fileId;
    public final String requestId;
    private final ZdaiHttpClient client;

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

    public ExtractionRequest(ZdaiHttpClient client, String fileId, String[] fieldIds) throws Exception {
        this.client = client;
        this.fileId = fileId;


        Response<String> response = client.authorizedRequest("POST",
                "/extraction",
                client.mapper.writeValueAsString(new ExtractionRequestBody(new String[]{fileId}, fieldIds)),
                202);

        ExtractionStatuses resp = client.mapper.readValue(response.getBody(), ExtractionStatuses.class);
        this.requestId = resp.statuses[0].requestId;
    }

    public String getStatus() throws Exception {
        Response<String> response = client.authorizedRequest("GET", String.format("/extraction/%s", requestId), 200);

        return client.mapper.readValue(response.getBody(), ExtractionStatus.class).status;
    }

    public ExtractionResults[] getResults() throws Exception {
        Response<String> response = client.authorizedRequest("GET", "/extraction/" + requestId + "/results/text", 200);
            return client.mapper.readValue(response.getBody(), ExtractionResultsBody.class).results;
    }
}
