package ai.zuva.docai.exception;

public class DocAIClientException extends DocAIException {
  public DocAIClientException(String errorMessage) {
    super(errorMessage);
  }

  public DocAIClientException(String errorMessage, Throwable err) {
    super(errorMessage, err);
  }
}
