package ai.zuva.files;

import ai.zuva.ZdaiClient;
import ai.zuva.exception.ZdaiApiException;
import ai.zuva.exception.ZdaiClientException;
import ai.zuva.http.ZdaiHttpClient;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.FileNotFoundException;
import java.nio.file.Path;

public class ZdaiFile {
    private ZdaiHttpClient client;
    public String fileId;
    public FileAttributes attributes;
    public String[] permissions;
    public String expiration;

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class SubmitFileResponse {
        @JsonProperty("file_id")
        public String fileId;
        @JsonProperty("attributes")
        public FileAttributes attributes;
        @JsonProperty("permissions")
        public String[] permissions;
        @JsonProperty("expiration")
        public String expiration;
    }

    public ZdaiFile(ZdaiHttpClient client, String fileId) {
        this.client = client;
        this.fileId = fileId;
    }

    public ZdaiFile(ZdaiHttpClient client, SubmitFileResponse resp) {
        this.client = client;
        this.fileId = resp.fileId;
        this.attributes = resp.attributes;
        this.permissions = resp.permissions;
        this.expiration= resp.expiration;
    }

    public static ZdaiFile submitFile(ZdaiHttpClient client, Path p, String... contentType) throws ZdaiClientException, ZdaiApiException, FileNotFoundException, SecurityException {
        return parseResponse(client, client.authorizedRequest("POST", "/files", p, 201, contentType));
    }

    public static ZdaiFile submitFile(ZdaiHttpClient client, String s, String... contentType) throws ZdaiClientException, ZdaiApiException {
        return parseResponse(client, client.authorizedRequest("POST", "/files", s, 201, contentType));
    }

    public static ZdaiFile submitFile(ZdaiHttpClient client, byte[] ba, String... contentType) throws ZdaiClientException, ZdaiApiException {
        return parseResponse(client, client.authorizedRequest("POST", "/files", ba, 201, contentType));
    }
    private static ZdaiFile parseResponse (ZdaiHttpClient client, String response) throws ZdaiClientException {
        try {
            SubmitFileResponse resp = client.mapper.readValue(response, SubmitFileResponse.class);
            return new ZdaiFile(client, resp);
        } catch (JsonProcessingException e) {
            throw new ZdaiClientException("Unable to parse response body", e);
        }
    }

    public void delete() throws ZdaiClientException, ZdaiApiException {
        client.authorizedRequest("DELETE", "/files/" + fileId, 204);
    }
}
