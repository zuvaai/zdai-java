package ai.zuva.exception;

import ai.zuva.fields.TrainingRequest;
import ai.zuva.http.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ZdaiClientException extends Exception {
    public ZdaiClientException(String errorMessage) {
        super(errorMessage);
    }
    public ZdaiClientException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
