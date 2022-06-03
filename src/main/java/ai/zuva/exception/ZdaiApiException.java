package ai.zuva.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpResponse;

public class ZdaiApiException extends Exception {
    public final int statusCode;
    public final String method;
    public final String uri;
    public final String body;

    public final String code;
    public final String message;

    static class ErrorBody {
        @JsonProperty("error")
        public ZdaiError error;
    }

    public ZdaiApiException(ObjectMapper mapper, String method, String uri, int statusCode, String body) {
        super(String.format("%s %s failed with status code %s.\nMessage:\n%s", method, uri, statusCode, body));
        this.method = method;
        this.uri = uri;
        this.statusCode = statusCode;
        this.body = body;

        String code;
        String message;
        try {
            ErrorBody errorBody = mapper.readValue(body, ErrorBody.class);
            code = errorBody.error.code;
            message = errorBody.error.message;
        } catch (JsonProcessingException | NullPointerException e) {
            code = "Unrecognized response body";
            message = "Unrecognized response body";
        }
        this.code = code;
        this.message = message;
    }
}
