package ai.zuva.files;

import ai.zuva.exception.ZdaiApiException;
import ai.zuva.exception.ZdaiClientException;
import ai.zuva.api.ZdaiApiClient;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;
import java.io.FileNotFoundException;

public class ZdaiFile {
    private ZdaiApiClient client;
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

    public static ZdaiFile submitFile(ZdaiApiClient client, File f, String... contentType) throws ZdaiClientException, ZdaiApiException, FileNotFoundException, SecurityException {
        SubmitFileResponse resp = client.authorizedRequest("POST", "api/v2/files", f, 201, SubmitFileResponse.class, contentType);
        return new ZdaiFile(client, resp);
    }

    public static ZdaiFile submitFile(ZdaiApiClient client, String s, String... contentType) throws ZdaiClientException, ZdaiApiException {
        SubmitFileResponse resp = client.authorizedRequest("POST", "api/v2/files", s, 201, SubmitFileResponse.class, contentType);
        return new ZdaiFile(client, resp);
    }

    public static ZdaiFile submitFile(ZdaiApiClient client, byte[] ba, String... contentType) throws ZdaiClientException, ZdaiApiException {
        SubmitFileResponse resp = client.authorizedRequest("POST", "api/v2/files", ba, 201,SubmitFileResponse.class, contentType);
        return new ZdaiFile(client, resp);
    }

    public ZdaiFile(ZdaiApiClient client, String fileId) {
        this.client = client;
        this.fileId = fileId;
    }

    public ZdaiFile(ZdaiApiClient client, SubmitFileResponse resp) {
        this.client = client;
        this.fileId = resp.fileId;
        this.attributes = resp.attributes;
        this.permissions = resp.permissions;
        this.expiration= resp.expiration;
    }

    public void delete() throws ZdaiClientException, ZdaiApiException {
        client.authorizedDelete("api/v2/files/" + fileId, 204);
    }

    // Returns an array of the file IDs of the given files
    public static String[] toFileIdArray(ZdaiFile[] files) {
        String[] fileIds = new String[files.length];

        for (int i = 0; i < files.length; i++) {
            fileIds[i] = files[i].fileId;
        }
        return fileIds;
    }
}
