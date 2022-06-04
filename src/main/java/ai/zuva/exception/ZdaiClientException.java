package ai.zuva.exception;

public class ZdaiClientException extends Exception {
    public ZdaiClientException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
