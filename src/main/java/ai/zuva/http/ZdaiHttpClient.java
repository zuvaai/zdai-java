package ai.zuva.http;

import ai.zuva.exception.ZdaiClientException;
import ai.zuva.exception.ZdaiApiException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

import static java.net.http.HttpRequest.BodyPublishers.noBody;

public class ZdaiHttpClient {
    public final String baseURL;
    public final String token;
    public HttpClient client;
    public ObjectMapper mapper = new ObjectMapper();

    public ZdaiHttpClient(String baseURL, String token) {
        this.baseURL = baseURL;
        this.token = token;
        client = HttpClient.newHttpClient();
    }

    public URI createURI(String uri) {
        return URI.create(this.baseURL + uri);
    }

    private Response<String> authorizedRequest(String method, String uri, HttpRequest.BodyPublisher body, int expectedStatusCode, String[] contentType) throws ZdaiClientException, ZdaiApiException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(this.baseURL + uri))
                .header("Authorization", "Bearer " + token)
                .method(method, body);

        if (contentType.length == 1) {
            requestBuilder.header("Content-type", contentType[0]);
        }

        try {
            HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != expectedStatusCode) {
                throw new ZdaiApiException(mapper, method, uri, response.statusCode(), response.body());
            }
            return new Response<>(response.statusCode(), response.body());
        } catch (IOException | InterruptedException e) {
            throw new ZdaiClientException("Http request failed", e);
        }
    }

    public Response<String> authorizedRequest(String method, String uri, int expectedStatusCode) throws ZdaiClientException, ZdaiApiException {
        return authorizedRequest(method, uri, noBody(), expectedStatusCode, new String[]{});
    }

    public Response<String> authorizedRequest(String method, String uri, String body, int expectedStatusCode, String... contentType) throws ZdaiClientException, ZdaiApiException {
        return authorizedRequest(method, uri, HttpRequest.BodyPublishers.ofString(body), expectedStatusCode, contentType);
    }

    public Response<String> authorizedRequest(String method, String uri, byte[] body, int expectedStatusCode, String... contentType) throws ZdaiClientException, ZdaiApiException {
        return authorizedRequest(method, uri, HttpRequest.BodyPublishers.ofByteArray(body), expectedStatusCode, contentType);
    }
    public Response<String> authorizedRequest(String method, String uri, Path body, int expectedStatusCode, String... contentType) throws ZdaiClientException, ZdaiApiException, FileNotFoundException {
        return authorizedRequest(method, uri, HttpRequest.BodyPublishers.ofFile(body), expectedStatusCode, contentType);
    }

    public Response<byte[]> authorizedRequest(String uri, int expectedStatusCode) throws ZdaiClientException, ZdaiApiException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.baseURL + uri))
                .header("Authorization", "Bearer " + token)
                .build();

        try {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != expectedStatusCode) {
                throw new ZdaiApiException(mapper, "GET", uri, response.statusCode(), new String(response.body()));
            }
            return new Response<>(response.statusCode(), response.body());
        } catch (IOException | InterruptedException e) {
            throw new ZdaiClientException("Http request failed", e);
        }
    }
}
