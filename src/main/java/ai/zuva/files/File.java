package ai.zuva.files;

import ai.zuva.exception.DocAIApiException;
import ai.zuva.exception.DocAIClientException;
import ai.zuva.api.DocAIClient;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.FileNotFoundException;

public class File {
    private final DocAIClient client;
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

    public static File submitFile(DocAIClient client, java.io.File f, String... contentType) throws DocAIClientException, DocAIApiException, FileNotFoundException, SecurityException {
        SubmitFileResponse resp = client.authorizedRequest("POST", "api/v2/files", f, 201, SubmitFileResponse.class, contentType);
        return new File(client, resp);
    }

    public static File submitFile(DocAIClient client, String s, String... contentType) throws DocAIClientException, DocAIApiException {
        SubmitFileResponse resp = client.authorizedRequest("POST", "api/v2/files", s, 201, SubmitFileResponse.class, contentType);
        return new File(client, resp);
    }

    public static File submitFile(DocAIClient client, byte[] ba, String... contentType) throws DocAIClientException, DocAIApiException {
        SubmitFileResponse resp = client.authorizedRequest("POST", "api/v2/files", ba, 201,SubmitFileResponse.class, contentType);
        return new File(client, resp);
    }

    public File(DocAIClient client, String fileId) {
        this.client = client;
        this.fileId = fileId;
    }

    public File(DocAIClient client, SubmitFileResponse resp) {
        this.client = client;
        this.fileId = resp.fileId;
        this.attributes = resp.attributes;
        this.permissions = resp.permissions;
        this.expiration= resp.expiration;
    }

    public void delete() throws DocAIClientException, DocAIApiException {
        client.authorizedDelete("api/v2/files/" + fileId, 204);
    }

    // Returns an array of the file IDs of the given files
    public static String[] toFileIdArray(File[] files) {
        String[] fileIds = new String[files.length];

        for (int i = 0; i < files.length; i++) {
            fileIds[i] = files[i].fileId;
        }
        return fileIds;
    }
}
