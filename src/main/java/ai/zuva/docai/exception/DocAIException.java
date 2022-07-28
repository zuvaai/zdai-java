package ai.zuva.docai.exception;

public class DocAIException extends Exception {
  public DocAIException() {
    super();
  }

  public DocAIException(String message) {
    super(message);
  }

  public DocAIException(String message, Throwable e) {
    super(message, e);
  }
}
