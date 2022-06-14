package ai.zuva.api;

import ai.zuva.exception.ZdaiClientException;
import ai.zuva.exception.ZdaiApiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ZdaiApiClient {
    private final HttpUrl baseUrl;
    private final String token;
    private final OkHttpClient client;
    private final ObjectMapper mapper;

    /**
     * Constructs a ZdaiApiClient to interface with the Zuva DocAI API
     *
     * @param baseUrl The url to make requests to (e.g. us.app.zuva.ai). The scheme and port may optionally be included.
     * @param token The Zuva token to use to authenticate all requests
     */
    public ZdaiApiClient(String baseUrl, String token) {
        this.token = token;
        client = new OkHttpClient();
        mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);

        // Set default scheme to https
        String scheme = "https";
        try{
            scheme = HttpUrl.parse(baseUrl).scheme();
        } catch (NullPointerException ignored){};

        this.baseUrl = HttpUrl.parse(baseUrl).newBuilder()
                .scheme(scheme)
                .build();
    }

    private HttpUrl buildUrl(String path){
        return baseUrl.newBuilder().addPathSegments(path).build();
    }

    private String JsonToStringBody(Object obj) throws ZdaiClientException {
        try {
            return this.mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Error creating request body", e));
        }
    }

    private <T> T jsonResponseToObject(String s, Class<T> type) throws ZdaiClientException {
        try {
            return mapper.readValue(s, type);
        } catch (JsonProcessingException e) {
            throw (new ZdaiClientException("Unable to parse response", e));
        }
    }

    private static MediaType toMediaType(String... contentType) {
        MediaType mediaType = null;
        if (contentType.length > 0) {
            mediaType = MediaType.parse(contentType[0]);
        }
        return mediaType;
    }

    private String sendRequest(Request request, int expectedStatusCode) throws ZdaiClientException, ZdaiApiException {
        try {
            Response response = client.newCall(request).execute();
            if (response.code() != expectedStatusCode) {
                throw new ZdaiApiException(mapper, request.method(), request.url().toString(), response.code(), response.body().string());
            }
            return response.body().string();
        } catch (IOException e) {
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
     * @param path The path part of the URI to send the request to
     * @param expectedStatusCode The status code expected for a successful response
     * @return The response body as a String, if the request was successful
     * @throws ZdaiClientException There was a problem sending the request, such as an IOException or InterruptedException
     * @throws ZdaiApiException The status code in the response was anything other than expectedStatusCode
     */
    public <T> T authorizedGet(String path, int expectedStatusCode, Class<T> type) throws ZdaiClientException, ZdaiApiException {
        Request request = new Request.Builder()
                .url(buildUrl(path))
                .header("Authorization", "Bearer " + token)
                .get()
                .build();
        return jsonResponseToObject(sendRequest(request, expectedStatusCode), type);
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
     * @return The response body as a String, if the request was successful
     * @throws ZdaiClientException There was a problem sending the request, such as an IOException or InterruptedException
     * @throws ZdaiApiException The status code in the response was anything other than expectedStatusCode
     */
    public String authorizedDelete(String path, int expectedStatusCode) throws ZdaiClientException, ZdaiApiException {
        Request request = new Request.Builder()
                .url(buildUrl(path))
                .header("Authorization", "Bearer " + token)
                .delete()
                .build();
        return sendRequest(request, expectedStatusCode);
    }

    // Shared functionality of the requests which do have bodies
    private <T> T authorizedRequest(String method, String path, RequestBody body, int expectedStatusCode, Class<T> responseType) throws ZdaiClientException, ZdaiApiException {
        Request.Builder builder = new Request.Builder()
                .url(buildUrl(path))
                .header("Authorization", "Bearer " + token)
                .method(method, body);

        String response = sendRequest(builder.build(), expectedStatusCode);
        if (responseType != null) {
            return jsonResponseToObject(response, responseType);
        } else {
            return null;
        }
    }

    /**
     * Makes an authorized Zuva API request with a JSON body, returning the body of the (successful) response as and object of type responseType
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
     * @return The response body as a String, if the request was successful
     * @throws ZdaiClientException There was a problem sending the request, such as an IOException or InterruptedException
     * @throws ZdaiApiException The status code in the response was anything other than expectedStatusCode
     */
    public <T> T authorizedJsonRequest(String method, String path, Object body, int expectedStatusCode, Class<T> responseType) throws ZdaiClientException, ZdaiApiException {
        String stringBody = this.JsonToStringBody(body);
        RequestBody requestBody = RequestBody.create(stringBody, null);
        return authorizedRequest(method, path, requestBody, expectedStatusCode, responseType);
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
    public <T> T authorizedRequest(String method, String path, String body, int expectedStatusCode, Class<T> responseType, String... contentType) throws ZdaiClientException, ZdaiApiException {
        RequestBody requestBody = RequestBody.create(body, toMediaType(contentType));
        return authorizedRequest(method, path, requestBody, expectedStatusCode, responseType);
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
    public <T> T authorizedRequest(String method, String path, byte[] body, int expectedStatusCode, Class<T> responseType, String... contentType) throws ZdaiClientException, ZdaiApiException {
        RequestBody requestBody = RequestBody.create(body, toMediaType(contentType));
        return authorizedRequest(method, path, requestBody, expectedStatusCode, responseType);
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
     * @throws FileNotFoundException The File to be used as the request body could not be found
     */
    public <T> T authorizedRequest(String method, String path, File body, int expectedStatusCode,  Class<T> responseType, String... contentType) throws ZdaiClientException, ZdaiApiException, FileNotFoundException, SecurityException {
        Request.Builder builder = new Request.Builder()
                .url(buildUrl(path))
                .header("Authorization", "Bearer " + token)
                .method(method, RequestBody.create(body, toMediaType(contentType)));

        String response = sendRequest(builder.build(), expectedStatusCode);
        return jsonResponseToObject(response, responseType);
    }

    /**
     * Makes an authorized GET request, returning the body of the (successful) response as a Byte array.
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
    public byte[] authorizedGetBinary(String path, int expectedStatusCode) throws ZdaiClientException, ZdaiApiException {
        Request request = new Request.Builder()
                .url(buildUrl(path))
                .header("Authorization", "Bearer " + token)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.code() != expectedStatusCode) {
                throw new ZdaiApiException(mapper, "GET", path, response.code(), response.body().string());
            }
            return response.body().bytes();
        } catch (IOException e) {
            throw new ZdaiClientException("Http request failed", e);
        }
    }
}
