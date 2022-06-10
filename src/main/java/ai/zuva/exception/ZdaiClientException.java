package ai.zuva.exception;

public class ZdaiClientException extends ZdaiException {
    public ZdaiClientException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
