package ai.zuva.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DocAIApiException extends DocAIException {
    public final int statusCode;
    public final String method;
    public final String uri;
    public final String body;

    // code and message are included in the body of some error response from the API
    public final String code;
    public final String message;

    // The standard error response defined in the Zuva DocAI api
    // See https://zuva.ai/documentation/using-the-apis/error-handling/
    static class ErrorBody {
        @JsonProperty("error")
        public DocAIError error;
    }

    public DocAIApiException(ObjectMapper mapper, String method, String uri, int statusCode, String body) {
        super(String.format("%s %s failed with status code %s.%nMessage:%n%s", method, uri, statusCode, body));

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
            code = "Could not find code in response body";
            message = "Cound not find message in response body";
        }
        this.code = code;
        this.message = message;
    }
}
