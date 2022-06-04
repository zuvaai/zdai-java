package ai.zuva.files;

import ai.zuva.exception.ZdaiApiException;
import ai.zuva.exception.ZdaiClientException;
import ai.zuva.http.ZdaiHttpClient;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.FileNotFoundException;
import java.nio.file.Path;

public class FileService {
    private ZdaiHttpClient client;

    public FileService(ZdaiHttpClient client) {
        this.client = client;
    }

    public SubmitFileResponse submitFile(Path p, String... contentType) throws ZdaiClientException, ZdaiApiException, FileNotFoundException, SecurityException {
        return parseResponse(client.authorizedRequest("POST", "/files", p, 201, contentType));
    }

    public SubmitFileResponse submitFile(String s, String... contentType) throws ZdaiClientException, ZdaiApiException {
        return parseResponse(client.authorizedRequest("POST", "/files", s, 201, contentType));
    }

    public SubmitFileResponse submitFile(byte[] ba, String... contentType) throws ZdaiClientException, ZdaiApiException {
        return parseResponse(client.authorizedRequest("POST", "/files", ba, 201, contentType));
    }
    private SubmitFileResponse parseResponse (String response) throws ZdaiClientException {
        try {
            return client.mapper.readValue(response, SubmitFileResponse.class);
        } catch (JsonProcessingException e) {
            throw new ZdaiClientException("Unable to parse response body", e);
        }
    }

    public void deleteFile(String fileID) throws ZdaiClientException, ZdaiApiException {
        client.authorizedRequest("DELETE", "/files/" + fileID, 204);
    }
}
