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

    /**
     * Constructs a ZdaiHttpClient to interface with the Zuva DocAI API
     *
     * @param baseURL The url to make requests to (e.g. https://us.app.zuva.ai)
     * @param token The Zuva token to use to authenticate all requests
     */
    public ZdaiHttpClient(String baseURL, String token) {
        this.baseURL = baseURL;
        this.token = token;
        client = HttpClient.newHttpClient();
    }

    private String authorizedRequest(String method, String uri, HttpRequest.BodyPublisher body, int expectedStatusCode, String[] contentType) throws ZdaiClientException, ZdaiApiException {
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
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new ZdaiClientException("Http request failed", e);
        }
    }

    /**
     * Makes an authorized Zuva API request, returning the body of the (successful) response as a String.
     * <p>
     * This function makes a request using the specified HTTP method to the specified URI (comprised of the Client's
     * baseURL + the given path), adding the required authorization header (using the client's token). If the status
     * code of the response matches expectedStatusCode, the response body is returned as a String. Otherwise, a
     * ZdaiApiException is thrown.
     *
     * @param method The HTTP method to use
     * @param path The path part of the URI to send the request to
     * @param expectedStatusCode The status code expected for a successful response
     * @return The response body as a String, if the request was successful
     * @throws ZdaiClientException There was a problem sending the request, such as an IOException or InterruptedException
     * @throws ZdaiApiException The status code in the response was anything other than expectedStatusCode
     */
    public String authorizedRequest(String method, String path, int expectedStatusCode) throws ZdaiClientException, ZdaiApiException {
        return authorizedRequest(method, path, noBody(), expectedStatusCode, new String[]{});
    }

    /**
     * Makes an authorized Zuva API request, returning the body of the (successful) response as a String.
     * <p>
     * This function makes a request using the specified HTTP method to the specified URI (comprised of the Client's
     * baseURL + the given path), adding the required authorization header (using the client's token). If the status
     * code of the response matches expectedStatusCode, the response body is returned as a String. Otherwise, a
     * ZdaiApiException is thrown.
     *
     * @param method The HTTP method to use
     * @param path The path part of the URI to send the request to
     * @param body The request body as a String
     * @param expectedStatusCode The status code expected for a successful response
     * @param contentType The MIME content type to specify in the request
     * @return The response body as a String, if the request was successful
     * @throws ZdaiClientException There was a problem sending the request, such as an IOException or InterruptedException
     * @throws ZdaiApiException The status code in the response was anything other than expectedStatusCode
     */
    public String authorizedRequest(String method, String path, String body, int expectedStatusCode, String... contentType) throws ZdaiClientException, ZdaiApiException {
        return authorizedRequest(method, path, HttpRequest.BodyPublishers.ofString(body), expectedStatusCode, contentType);
    }

    /**
     * Makes an authorized Zuva API request, returning the body of the (successful) response as a String.
     * <p>
     * This function makes a request using the specified HTTP method to the specified URI (comprised of the Client's
     * baseURL + the given path), adding the required authorization header (using the client's token). If the status
     * code of the response matches expectedStatusCode, the response body is returned as a String. Otherwise, a
     * ZdaiApiException is thrown.
     *
     * @param method The HTTP method to use
     * @param path The path part of the URI to send the request to
     * @param body The request body as a byte array
     * @param expectedStatusCode The status code expected for a successful response
     * @param contentType The MIME content type to specify in the request
     * @return The response body as a String, if the request was successful
     * @throws ZdaiClientException There was a problem sending the request, such as an IOException or InterruptedException
     * @throws ZdaiApiException The status code in the response was anything other than expectedStatusCode
     */
    public String authorizedRequest(String method, String path, byte[] body, int expectedStatusCode, String... contentType) throws ZdaiClientException, ZdaiApiException {
        return authorizedRequest(method, path, HttpRequest.BodyPublishers.ofByteArray(body), expectedStatusCode, contentType);
    }

    /**
     * Makes an authorized Zuva API request, returning the body of the (successful) response as a String.
     * <p>
     * This function makes a request using the specified HTTP method to the specified URI (comprised of the Client's
     * baseURL + the given path), adding the required authorization header (using the client's token). If the status
     * code of the response matches expectedStatusCode, the response body is returned as a String. Otherwise, a
     * ZdaiApiException is thrown.
     *
     * @param method The HTTP method to use
     * @param path The path part of the URI to send the request to
     * @param body A Path specifying a file to upload as the request body
     * @param expectedStatusCode The status code expected for a successful response
     * @param contentType The MIME content type to specify in the request
     * @return The response body as a String, if the request was successful
     * @throws ZdaiClientException There was a problem sending the request, such as an IOException or InterruptedException
     * @throws ZdaiApiException The status code in the response was anything other than expectedStatusCode
     */
    public String authorizedRequest(String method, String path, Path body, int expectedStatusCode, String... contentType) throws ZdaiClientException, ZdaiApiException, FileNotFoundException, SecurityException {
        return authorizedRequest(method, path, HttpRequest.BodyPublishers.ofFile(body), expectedStatusCode, contentType);
    }

    /**
     * Makes an authorized Zuva API request, returning the body of the (successful) response as a String.
     * <p>
     * This function makes a request using the specified HTTP method to the specified URI (comprised of the Client's
     * baseURL + the given path), adding the required authorization header (using the client's token). If the status
     * code of the response matches expectedStatusCode, the response body is returned as a String. Otherwise, a
     * ZdaiApiException is thrown.
     *
     * @param path The path part of the URI to send the request to
     * @param expectedStatusCode The status code expected for a successful response
     * @return The response body as a byte array, if the request was successful
     * @throws ZdaiClientException There was a problem sending the request, such as an IOException or InterruptedException
     * @throws ZdaiApiException The status code in the response was anything other than expectedStatusCode
     */
    public byte[] authorizedRequest(String path, int expectedStatusCode) throws ZdaiClientException, ZdaiApiException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.baseURL + path))
                .header("Authorization", "Bearer " + token)
                .build();

        try {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != expectedStatusCode) {
                throw new ZdaiApiException(mapper, "GET", path, response.statusCode(), new String(response.body()));
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new ZdaiClientException("Http request failed", e);
        }
    }
}
