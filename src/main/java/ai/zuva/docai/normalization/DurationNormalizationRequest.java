package ai.zuva.docai.normalization;

import ai.zuva.docai.DocAIClient;
import ai.zuva.docai.exception.DocAIApiException;
import ai.zuva.docai.exception.DocAIClientException;

public class DurationNormalizationRequest extends NormalizationRequest {
  /**
   * Sends a request for duration normalization
   *
   * @param client The client to use to make the request
   * @param text The text to normalize for duration
   * @return A DurationNormalizationResults, which contains the results of the duration
   *     normalization
   * @throws DocAIClientException Unsuccessful response code from server
   * @throws DocAIApiException Error preparing, sending or processing the request/response
   */
  public static DurationNormalizationResults createRequest(DocAIClient client, String text)
      throws DocAIClientException, DocAIApiException {
    DurationNormalizationResults resp =
        client.authorizedJsonRequest(
            "POST",
            "api/v2/normalization/duration",
            new NormalizationRequestBody(text),
            200,
            DurationNormalizationResults.class);
    return resp;
  }
}
