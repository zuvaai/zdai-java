package ai.zuva.exception;

public class DocAIClientException extends DocAIException {
    public DocAIClientException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}